package edu.pw.eiti.pik.project;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/project")
public class ProjectController
{

    private final ProjectMapper projectMapper = ProjectMapper.getInstance();
    @Autowired
    private ProjectService projectService;

    @PreAuthorize("hasRole('EMPLOYER')")
    @ApiOperation(value = "Add new project", notes = "Adds project into database")
    @PostMapping(path = "/project/add")
    @ApiParam(value = "Project details", required = true)
    void addProject(@RequestBody ProjectDto projectDto) {
        Project project = projectMapper.fromDto(projectDto);
        projectService.createProject(project);
    }
}
