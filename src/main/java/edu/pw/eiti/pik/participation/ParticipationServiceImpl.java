package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ParticipationServiceImpl implements ParticipationService {

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private ApplicationEventPublisher publisher;

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
    public void inviteUser(String inviterUsername, String invitedUsername, Long projectId, Boolean isTeacher) {
        Participation inviterParticipation = participationRepository
                                            .findByUser_EmailAndProject_Id(inviterUsername, projectId);
        if (inviterParticipation == null)
            throw new ParticipationNotFoundException();
        else if (!inviterParticipation.getStatus().equals(ParticipationStatus.OWNER)
                && !inviterParticipation.getStatus().equals(ParticipationStatus.MANAGER))
            throw new WrongParticipationStatusException();
        else {
            Participation participation = new Participation();
            participation.setStatus(ParticipationStatus.PENDING_INVITATION);
            publisher.publishEvent(new AddProjectToParticipationEvent(participation, invitedUsername, projectId));
        }
    }

    @Override
    public void acceptStudent(String authUsername, String acceptedUsername, Long projectId) {
        Participation authParticipation = participationRepository.findByUser_EmailAndProject_Id(authUsername, projectId);
        if (authParticipation == null)
            throw new ParticipationNotFoundException();
        else if (!authParticipation.getStatus().equals(ParticipationStatus.MANAGER) ||
                 !authParticipation.getStatus().equals(ParticipationStatus.OWNER))
            throw new WrongParticipationStatusException();
        else {
            Participation studentParticipation = participationRepository.findByUser_EmailAndProject_Id(acceptedUsername, projectId);
            studentParticipation.setStatus(ParticipationStatus.PARTICIPANT);
            participationRepository.save(studentParticipation);
        }
    }
}
