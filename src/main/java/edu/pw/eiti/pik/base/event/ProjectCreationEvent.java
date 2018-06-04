package edu.pw.eiti.pik.base.event;

import edu.pw.eiti.pik.project.Project;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectCreationEvent {
    private final Project project;
}
