package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.ProjectCreationEvent;
import org.springframework.context.event.EventListener;

public interface ParticipationService {
    @EventListener
    void setProjectOwner(ProjectCreationEvent event);

    void changeStatus(ParticipationStatus status, Long projectId, String username);

    void deleteParticipation(String username, Long projectId, Boolean isTeacher);

    void addParticipation(String username, Long projectId);

    void acceptInvitation(String username, Long projectId, Boolean isTeacher);

    void inviteUser(String inviterUsername, String invitedUsername, Long projectId);

    void acceptParticipant(String authUsername, String acceptedUsername, Long projectId, Boolean isTeacher);
}
