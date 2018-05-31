package edu.pw.eiti.pik.base.event;


import edu.pw.eiti.pik.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AddProjectToESEvent {
    private Project project;
}
