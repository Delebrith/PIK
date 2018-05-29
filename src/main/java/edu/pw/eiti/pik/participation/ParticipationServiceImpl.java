package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @EventListener
    @Transactional
    @Override
    public void setProjectOwner(ProjectCreationEvent event) {
        Participation participation = new Participation();
        participation.setProject(event.getProject());
        participation.setStatus(ParticipationStatus.OWNER);
        event.getProject().getParticipations().add(participation);
        publisher.publishEvent(new ParticipationCreationEvent(participation));
    }

    @Override
    public void changeStatus(ParticipationChangeStatus status, Long projectId, String username) {
        final String TEACHER = "TEACHER";
        final String STUDENT = "STUDENT";
        final String EMPLOYER = "EMPLOYER";
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        String authUsername = auth.getName();
        if (status.equals(ParticipationChangeStatus.RESIGN)
                || status.equals(ParticipationChangeStatus.REJECT_INVITATION)) {
            if (!authorities.contains(TEACHER))
                deleteParticipation(authUsername, projectId, false);
            else
                deleteParticipation(authUsername, projectId, true);
        } else if (status.equals(ParticipationChangeStatus.SIGN_UP)) {
            if (authorities.contains(TEACHER) || authorities.contains(STUDENT))
                addParticipation(authUsername, projectId);
        } else if (status.equals(ParticipationChangeStatus.ACCEPT_STUDENT)) {
            if (authorities.contains(EMPLOYER) && !username.equals("{}"))
                acceptParticipant(authUsername, username, projectId, false);
        } else if (status.equals(ParticipationChangeStatus.ACCEPT_TEACHER)) {
            if (authorities.contains(EMPLOYER) && !username.equals("{}"))
                acceptParticipant(authUsername, username, projectId, true);
        } else if (status.equals(ParticipationChangeStatus.INVITE)) {
            if (!authorities.contains(STUDENT) && !username.equals("{}"))
                inviteUser(authUsername, username, projectId);
        } else if (status.equals(ParticipationChangeStatus.ACCEPT_INVITATION)) {
            if (authorities.contains(STUDENT))
                acceptInvitation(authUsername, projectId, false);
            else if (authorities.contains(TEACHER))
                acceptInvitation(authUsername, projectId, true);
        }
    }

    @Override
    public void deleteParticipation(String username, Long projectId, Boolean isTeacher) {
        Participation participation = participationRepository
                                        .findByUser_EmailAndProject_Id(username, projectId);
        if (participation == null)
            throw new ParticipationNotFoundException();
        else if (participation.getStatus().equals(ParticipationStatus.PARTICIPANT) || participation.getStatus().equals(ParticipationStatus.WAITING_FOR_ACCEPTANCE)
            || participation.getStatus().equals(ParticipationStatus.PENDING_INVITATION) || participation.getStatus().equals(ParticipationStatus.MANAGER)) {
            participationRepository.delete(participation);
            publisher.publishEvent(new CheckParticipantsAfterDeletedEvent(projectId, isTeacher));
        }
        else if (participation.getStatus().equals(ParticipationStatus.OWNER)) {
            publisher.publishEvent(new CancelProjectEvent(projectId));
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
        Participation participation = participationRepository.findByUser_EmailAndProject_Id(username, projectId);
        if (participation == null || participation.getStatus() != ParticipationStatus.PENDING_INVITATION)
            throw new ParticipationNotFoundException();
        if (!isTeacher)
            participation.setStatus(ParticipationStatus.PARTICIPANT);
        else
            participation.setStatus(ParticipationStatus.MANAGER);
        participationRepository.save(participation);
    }

    @Override
    public void inviteUser(String inviterUsername, String invitedUsername, Long projectId) {
        Participation inviterParticipation = participationRepository
                                            .findByUser_EmailAndProject_Id(inviterUsername, projectId);
        if (inviterParticipation == null)
            throw new ParticipationNotFoundException();
        else if (!inviterParticipation.getStatus().equals(ParticipationStatus.OWNER) &&
                 !inviterParticipation.getStatus().equals(ParticipationStatus.MANAGER))
            throw new WrongParticipationStatusException();
        else {
            Participation participation = new Participation();
            participation.setStatus(ParticipationStatus.PENDING_INVITATION);
            publisher.publishEvent(new AddProjectToParticipationEvent(participation, invitedUsername, projectId));
        }
    }

    @Override
    public void acceptParticipant(String authUsername, String acceptedUsername, Long projectId, Boolean isTeacher) {
        Participation authParticipation = participationRepository.findByUser_EmailAndProject_Id(authUsername, projectId);
        if (authParticipation == null)
            throw new ParticipationNotFoundException();
        else if (!authParticipation.getStatus().equals(ParticipationStatus.MANAGER) &&
                 !authParticipation.getStatus().equals(ParticipationStatus.OWNER))
            throw new WrongParticipationStatusException();
        else {
            Participation acceptedParticipation = participationRepository.findByUser_EmailAndProject_Id(acceptedUsername, projectId);
            if (acceptedParticipation == null)
                throw new ParticipationNotFoundException();
            else if (!isTeacher)
                acceptedParticipation.setStatus(ParticipationStatus.PARTICIPANT);
            else
                acceptedParticipation.setStatus(ParticipationStatus.MANAGER);
            participationRepository.save(acceptedParticipation);
        }
    }
}
