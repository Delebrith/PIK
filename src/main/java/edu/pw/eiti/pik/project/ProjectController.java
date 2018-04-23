package edu.pw.eiti.pik.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/project")
public class ProjectController
{

    private final ProjectMapper projectMapper = ProjectMapper.getInstance();
    @Autowired
    private ProjectService projectService;

    @PostMapping(path = "/project/add")
    void addProject(@RequestBody ProjectDto projectDto) {
        Project project = projectMapper.fromDto(projectDto);
        projectService.createProject(project);
    }
}
