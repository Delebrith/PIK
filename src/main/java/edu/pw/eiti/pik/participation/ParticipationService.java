package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.ProjectCreationEvent;
import org.springframework.context.event.EventListener;

public interface ParticipationService {
    @EventListener
    void setProjectOwner(ProjectCreationEvent event);

    void deleteParticipation(String username, Long projectId, Boolean isTeacher);

    void addParticipation(String username, Long projectId, Boolean isTeacher);
}
