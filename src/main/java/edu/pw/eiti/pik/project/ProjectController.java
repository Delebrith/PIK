package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.base.config.web.ErrorDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController("/project")
public class ProjectController
{

    private final ProjectMapper projectMapper = ProjectMapper.getInstance();
    @Autowired
    private ProjectService projectService;

    @ApiOperation(value = "Add new project", notes = "Adds project into database")
    @ApiParam(value = "Project details", required = true)
    @ApiResponses({
            @ApiResponse(code = 200, message = "If project was successfully added"),
            @ApiResponse(code = 403, message = "If user is not authorized to perform this operation", response = ErrorDto.class)
    })
    @PreAuthorize("hasAuthority('EMPLOYER')")
    @PostMapping(path = "/project/add")
    void addProject(@RequestBody ProjectDto projectDto, @RequestParam(required = false) String teacherMail) {
        Project project = projectMapper.fromDto(projectDto);
        projectService.createProject(project, teacherMail);
    }
}
