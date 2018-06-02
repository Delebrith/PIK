package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.*;
import edu.pw.eiti.pik.user.Authorities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;

    private final ApplicationEventPublisher publisher;

    @Autowired
    public ParticipationServiceImpl(ParticipationRepository participationRepository, ApplicationEventPublisher publisher) {
        this.participationRepository = participationRepository;
        this.publisher = publisher;
    }

    @Override
    public void changeStatus(ParticipationStatus status, Long projectId, String username) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        String authUsername = auth.getName();
        if (status.equals(ParticipationStatus.RESIGNED)) {
            if (!authorities.contains(Authorities.TEACHER.toString()))
                deleteParticipation(authUsername, projectId, false);
            else
                deleteParticipation(authUsername, projectId, true);
        }
        else if (status.equals(ParticipationStatus.WAITING_FOR_ACCEPTANCE)) {
            if (authorities.contains(Authorities.TEACHER.toString()) || authorities.contains(Authorities.STUDENT.toString()))
                addParticipation(authUsername, projectId);
        }
        else if (status.equals(ParticipationStatus.PARTICIPANT)) {
            if (authorities.contains(Authorities.STUDENT.toString()))
                acceptInvitation(authUsername, projectId, false);
            else if (authorities.contains(Authorities.EMPLOYER.toString()) && username != null)
                acceptParticipant(authUsername, username, projectId, false);
        }
        else if (status.equals(ParticipationStatus.MANAGER)) {
            if (authorities.contains(Authorities.TEACHER.toString()))
                acceptInvitation(authUsername, projectId, true);
            else if (authorities.contains(Authorities.EMPLOYER.toString()) && username != null)
                acceptParticipant(authUsername, username, projectId, true);
        }
        else if (status.equals(ParticipationStatus.PENDING_INVITATION)) {
            if (!authorities.contains(Authorities.STUDENT.toString()) && username != null)
                inviteUser(authUsername, username, projectId);
        }
    }

    @Override
    public void deleteParticipation(String username, Long projectId, Boolean isTeacher) {
        List<Participation> participation = participationRepository
                                        .findByUser_EmailAndProject_Id(username, projectId);
        if (participation.isEmpty())
            throw new ParticipationNotFoundException();
        for (Participation p : participation) {
	        if (p.getStatus().equals(ParticipationStatus.PARTICIPANT) || p.getStatus().equals(ParticipationStatus.WAITING_FOR_ACCEPTANCE)
	            || p.getStatus().equals(ParticipationStatus.PENDING_INVITATION) || p.getStatus().equals(ParticipationStatus.MANAGER)) {
	            participationRepository.delete(p);
	            publisher.publishEvent(new CheckParticipantsAfterDeletedEvent(projectId, isTeacher));
	        }
	        else if (p.getStatus().equals(ParticipationStatus.OWNER)) {
	            publisher.publishEvent(new CancelProjectEvent(projectId));
	        }
        }
    }

    @Override
    public void addParticipation(String username, Long projectId) {
        Participation participation = new Participation();
        participation.setStatus(ParticipationStatus.WAITING_FOR_ACCEPTANCE);
        publisher.publishEvent(new AddProjectToParticipationEvent(participation, username, projectId));
    }

    @Override
    public void acceptInvitation(String username, Long projectId, Boolean isTeacher) {
        List<Participation> participation = participationRepository.findByUser_EmailAndProject_Id(username, projectId);
        if (participation.isEmpty() || participation.get(0).getStatus() != ParticipationStatus.PENDING_INVITATION)
            throw new ParticipationNotFoundException();
        if (!isTeacher)
            participation.get(0).setStatus(ParticipationStatus.PARTICIPANT);
        else
            participation.get(0).setStatus(ParticipationStatus.MANAGER);
        participationRepository.saveAll(participation);
    }

    @Override
    public void inviteUser(String inviterUsername, String invitedUsername, Long projectId) {
        List<Participation> inviterParticipation = participationRepository
                                            .findByUser_EmailAndProject_Id(inviterUsername, projectId);
        if (inviterParticipation.isEmpty())
            throw new ParticipationNotFoundException();
        
        boolean isAuthorizedToAccept = false;
        for (Participation p : inviterParticipation)
        	if (p.getStatus().equals(ParticipationStatus.MANAGER) ||
                 p.getStatus().equals(ParticipationStatus.OWNER))
        		isAuthorizedToAccept = true;
        if (!isAuthorizedToAccept)
            throw new WrongParticipationStatusException();
        
        else {
            Participation participation = new Participation();
            participation.setStatus(ParticipationStatus.PENDING_INVITATION);
            publisher.publishEvent(new AddProjectToParticipationEvent(participation, invitedUsername, projectId));
        }
    }

    @Override
    public void acceptParticipant(String authUsername, String acceptedUsername, Long projectId, Boolean isTeacher) {
        List<Participation> authParticipation = participationRepository.findByUser_EmailAndProject_Id(authUsername, projectId);
        if (authParticipation == null)
            throw new ParticipationNotFoundException();
        boolean isAuthorizedToAccept = false;
        for (Participation p : authParticipation)
        	if (p.getStatus().equals(ParticipationStatus.MANAGER) ||
                 p.getStatus().equals(ParticipationStatus.OWNER))
        		isAuthorizedToAccept = true;
        if (!isAuthorizedToAccept)
            throw new WrongParticipationStatusException();
        else {
            List<Participation> acceptedParticipation = participationRepository.findByUser_EmailAndProject_Id(acceptedUsername, projectId);
            if (acceptedParticipation == null)
                throw new ParticipationNotFoundException();
            for (Participation p : acceptedParticipation) {
	            if (!isTeacher)
	                p.setStatus(ParticipationStatus.PARTICIPANT);
	            else
	                p.setStatus(ParticipationStatus.MANAGER);
            }
            participationRepository.saveAll(acceptedParticipation);
        }
    }

    @Override
    @EventListener
    public void addNewParticipation(UserAndProjectToParticipationEvent event) {
        Participation participation = new Participation();
        participation.setStatus(event.getStatus());
        participation.setUser(event.getUser());
        participation.setProject(event.getProject());
        Participation savedParticipation = participationRepository.save(participation);
        publisher.publishEvent(new AddProjectToESEvent(savedParticipation.getProject()));
    }
    
    @Override
	public List<Participation> findByUser_EmailAndProject_Id(String username, Long projectId) {
    	return participationRepository.findByUser_EmailAndProject_Id(username, projectId);
	}
}
