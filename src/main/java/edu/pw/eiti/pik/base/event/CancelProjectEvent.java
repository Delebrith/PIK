package edu.pw.eiti.pik.base.event;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CancelProjectEvent {

    private Long projectId;
}
