package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.base.config.web.ErrorDto;
import io.swagger.annotations.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController("/project")
@RequestMapping("/project")
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
    @PostMapping(path = "/add")
    ResponseEntity addProject(@RequestBody ProjectDto projectDto, @RequestParam(required = false) String teacherMail) {
        Project project = projectMapper.fromDto(projectDto);
        projectService.createProject(project, teacherMail);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Search for projects")
    @ApiResponse(code = 200, message = "Always")
    @GetMapping(path = "/find/{status}/{pageSize}/{page}")
    List<ProjectDto> findProjects(@RequestParam(name="query", required=false) String phrase,
    		@PathVariable ProjectStatus status, @PathVariable int pageSize, @PathVariable int page) {
    	Page<Project> queryResult;
    	if (phrase == null || phrase.isEmpty())
			queryResult = projectService.findProjectsByStatus(status, PageRequest.of(page, pageSize));
    	else
    		queryResult = projectService.findProjectsByPhraseAndStatus(phrase, status, PageRequest.of(page, pageSize));


    	return queryResult.stream().map(projectMapper::toDto).collect(Collectors.toList());
    }

    @ApiOperation(value = "Modify project settings")
    @PostMapping(path = "/change/{projectId}")
    @ApiResponses({
            @ApiResponse(code = 202, message = "When changes to project are accepted"),
            @ApiResponse(code = 401, message = "When user is not authorized to make a change"),
            @ApiResponse(code = 400, message = "When given projects setting are illogical or incorrect")
    })
    @PreAuthorize("hasAuthority('EMPLOYER')")
    ResponseEntity changeProjectSettings(@PathVariable Long projectId,
                                         @RequestParam(required = false) String name,
                                         @RequestParam(required = false) String description,
                                         @RequestParam(required = false) Integer numOfParticipants,
                                         @RequestParam(required = false) Integer minimumPay,
                                         @RequestParam(required = false) Integer maximumPay,
                                         @RequestParam(required = false) Integer ects,
                                         @RequestParam(required = false) Boolean isGraduateWork) {
        projectService.changeSettings(projectId, name, description, numOfParticipants, minimumPay, maximumPay, ects, isGraduateWork);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }
}
