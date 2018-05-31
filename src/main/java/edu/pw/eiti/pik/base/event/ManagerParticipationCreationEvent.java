package edu.pw.eiti.pik.base.event;


import edu.pw.eiti.pik.participation.Participation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ManagerParticipationCreationEvent {

    private final Participation participation;
    private final String teacherMail;

}
