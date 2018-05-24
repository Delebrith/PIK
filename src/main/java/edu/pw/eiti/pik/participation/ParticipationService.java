package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.ProjectCreationEvent;

public interface ParticipationService {
    void setProjectOwner(ProjectCreationEvent event);
}
