package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.base.config.web.ErrorDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController("/project")
public class ProjectController
{

    private final ProjectMapper projectMapper = ProjectMapper.getInstance();
    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @ApiOperation(value = "Add new project", notes = "Adds project into database")
    @ApiParam(value = "Project details", required = true)
    @ApiResponses({
            @ApiResponse(code = 201, message = "If project was successfully added"),
            @ApiResponse(code = 400, message = "If user provided user without TEACHER authority or project settings are incorrect"
                    , response = ErrorDto.class),
            @ApiResponse(code = 404, message = "If provided teacher was not found", response = ErrorDto.class)
    })
    @PreAuthorize("hasAuthority('EMPLOYER')")
    @PostMapping(path = "/project/add")
    ResponseEntity addProject(@RequestBody ProjectDto projectDto, @RequestParam(required = false) String teacherMail) {
        Project project = projectMapper.fromDto(projectDto);
        projectService.createProject(project, teacherMail);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Search for projects")
    @ApiResponse(code = 200, message = "Always")
    @GetMapping(path = "/project/find/{status}/{pageSize}/{page}")
    List<ProjectDto> findProjects(@RequestParam(name="query", required=false) String phrase,
    		@PathVariable ProjectStatus status, @PathVariable int pageSize, @PathVariable int page) {
    	Page<Project> queryResult;
    	if (phrase == null || phrase.isEmpty())
			queryResult = projectService.findProjectsByStatus(status, PageRequest.of(page, pageSize));
    	else
    		queryResult = projectService.findProjectsByPhraseAndStatus(phrase, status, PageRequest.of(page, pageSize));


    	return queryResult.stream().map(projectMapper::toDto).collect(Collectors.toList());
    }

    @ApiOperation(value = "Search for currently authentcated user's projects projects")
    @ApiResponse(code = 200, message = "Always")
    @GetMapping(path = "/project/my/{pageNumber}/{pageSize}")
    List<ProjectDto> findMyProjects(@PathVariable Integer pageNumber,
                                    @PathVariable Integer pageSize) {
        return projectService.findMyProjects(pageNumber, pageSize)
                .stream().map(projectMapper::toDto).collect(Collectors.toList());
    }
}
