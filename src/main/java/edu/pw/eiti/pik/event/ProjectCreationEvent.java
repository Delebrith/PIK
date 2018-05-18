package edu.pw.eiti.pik.event;

import edu.pw.eiti.pik.project.Project;
import edu.pw.eiti.pik.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectCreationEvent {

    private final Project project;
    private final String email;

}
