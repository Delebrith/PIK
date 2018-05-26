package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.ProjectCreationEvent;
import org.springframework.context.event.EventListener;

public interface ParticipationService {
    @EventListener
    void setProjectOwner(ProjectCreationEvent event);

    void deleteParticipation(String username, Long projectId, Boolean isTeacher);

    void addParticipation(String username, Long projectId);

    void acceptInvitation(String username, Long projectId, Boolean isTeacher);

    void inviteUser(String inviterUsername, String invitedUsername, Long projectId, Boolean isTeacher);

    void acceptStudent(String authUsername, String acceptedUsername, Long projectId);
}
