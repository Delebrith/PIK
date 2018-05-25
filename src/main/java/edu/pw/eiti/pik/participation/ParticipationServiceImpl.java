package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.AddProjectToParticipationEvent;
import edu.pw.eiti.pik.base.event.CheckParticipantsAfterDeletedEvent;
import edu.pw.eiti.pik.base.event.ParticipationCreationEvent;
import edu.pw.eiti.pik.base.event.ProjectCreationEvent;
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
        publisher.publishEvent(new ParticipationCreationEvent(participation));
    }

    @Override
    public void deleteParticipation(String username, Long projectId, Boolean isTeacher) {
        Participation participation = participationRepository
                                        .findByUser_NameAndProject_Id(username, projectId);
        if (participation.getStatus().equals(ParticipationStatus.PARTICIPANT) || participation.getStatus().equals(ParticipationStatus.WAITING_FOR_ACCEPTANCE)) {
            participationRepository.delete(participation);
            publisher.publishEvent(new CheckParticipantsAfterDeletedEvent(projectId, isTeacher));
        }
    }

    @Override
    public void addParticipation(String username, Long projectId, Boolean isTeacher) {
        if (!isTeacher) {
            Participation participation = new Participation();
            participation.setStatus(ParticipationStatus.WAITING_FOR_ACCEPTANCE);
            publisher.publishEvent(new AddProjectToParticipationEvent(participation, username, projectId));
        }
        else {
            Participation invitedTeacher = participationRepository.findByUser_NameAndProject_Id(username, projectId);
            if (invitedTeacher != null && invitedTeacher.getStatus().equals(ParticipationStatus.PENDING_INVITATION)) {
                invitedTeacher.setStatus(ParticipationStatus.PARTICIPANT);
                participationRepository.save(invitedTeacher);
            }
        }

    }
}
