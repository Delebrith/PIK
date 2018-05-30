package edu.pw.eiti.pik.base.event;

import edu.pw.eiti.pik.participation.Participation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddProjectToParticipationEvent {

    private Participation participation;
    private String username;
    private Long projectId;
}
