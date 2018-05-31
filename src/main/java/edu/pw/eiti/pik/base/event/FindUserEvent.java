package edu.pw.eiti.pik.base.event;

import edu.pw.eiti.pik.participation.ParticipationStatus;
import edu.pw.eiti.pik.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FindUserEvent {

    private Project project;
    private ParticipationStatus status;
    private String username;
}
