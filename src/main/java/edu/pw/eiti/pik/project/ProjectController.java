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
    @GetMapping(path = "/project/find/{pageSize}/{page}")
    List<ProjectDto> findProjects(@RequestParam(name="query", required=false) String phrase,
    		@RequestParam(name="status", required=false) List<ProjectStatus> statuses,
    		@RequestParam(name="min-ects", required=false, defaultValue="0") int minEcts,
    		@RequestParam(name="min-pay", required=false, defaultValue="0") int minPay,
    		@RequestParam(name="only-grad-work", required=false, defaultValue="0") boolean onlyGraduateWork,
    		@PathVariable int pageSize, @PathVariable int page) {
    	if (statuses == null) {
			statuses = new ArrayList<ProjectStatus>();
			statuses.add(ProjectStatus.CREATED);
			statuses.add(ProjectStatus.WAITING_FOR_STUDENTS);
			statuses.add(ProjectStatus.STARTED);
			statuses.add(ProjectStatus.SUSPENDED_MISSING_PARTICIPANTS);
			statuses.add(ProjectStatus.SUSPENDED_MISSING_TEACHER);
			statuses.add(ProjectStatus.FINISHED);
			statuses.add(ProjectStatus.OVERDUE);
		}
		
    	Page<Project> queryResult;
    	if (phrase == null || phrase.isEmpty())
			queryResult = projectService.findProjectsWhereStatusInStatuses(
					statuses, minEcts, minPay, onlyGraduateWork, PageRequest.of(page, pageSize));
    	else
    		queryResult = projectService.findProjectsByPhraseAndStatus(
    				phrase, statuses, minEcts, minPay, onlyGraduateWork, PageRequest.of(page, pageSize));


    	return queryResult.stream().map(projectMapper::toDto).collect(Collectors.toList());
    }
}
