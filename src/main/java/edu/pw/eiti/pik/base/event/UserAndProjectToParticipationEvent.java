package edu.pw.eiti.pik.base.event;


import edu.pw.eiti.pik.participation.ParticipationStatus;
import edu.pw.eiti.pik.project.Project;
import edu.pw.eiti.pik.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserAndProjectToParticipationEvent {

    private Project project;
    private User user;
    private ParticipationStatus status;

}
