package edu.pw.eiti.pik.base.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CheckProjectStatusEvent {

    private Long projectId;

}
