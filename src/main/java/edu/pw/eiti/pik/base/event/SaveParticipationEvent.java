package edu.pw.eiti.pik.base.event;

import edu.pw.eiti.pik.participation.Participation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SaveParticipationEvent {

    Participation participation;
}
