package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.base.config.web.ErrorDto;
import io.swagger.annotations.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController("/project")
public class ProjectController
{
	private final static List<ProjectStatus> DEFAULT_PROJECT_STATUSES = new ArrayList<ProjectStatus>(){{
			add(ProjectStatus.CREATED);
			add(ProjectStatus.WAITING_FOR_STUDENTS);
			add(ProjectStatus.STARTED);
			add(ProjectStatus.SUSPENDED_MISSING_PARTICIPANTS);
			add(ProjectStatus.SUSPENDED_MISSING_TEACHER);
			add(ProjectStatus.FINISHED);
			add(ProjectStatus.OVERDUE);
	}};

    private final ProjectMapper projectMapper = ProjectMapper.getInstance();
    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }


    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(ProjectStatus.class, new ProjectStatusConverter());
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
			statuses.addAll(DEFAULT_PROJECT_STATUSES);
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

    @ApiOperation(value = "Modify project settings")
    @PostMapping(path = "/project/change/{projectId}")
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

    @ApiOperation(value = "Search for currently authentcated user's projects projects")
    @ApiResponse(code = 200, message = "Always")
    @GetMapping(path = "/project/my/{pageSize}/{pageNumber}")
    List<ProjectDto> findMyProjects(@PathVariable Integer pageSize,
            						@PathVariable Integer pageNumber,
                            		@RequestParam(name="status", required=false) List<ProjectStatus> statuses) {
    	if (statuses == null) {
			statuses = new ArrayList<ProjectStatus>();
			statuses.addAll(DEFAULT_PROJECT_STATUSES);
		}
    	
        return projectService.findMyProjects(pageSize, pageNumber, statuses)
                .stream().map(projectMapper::toDto).collect(Collectors.toList());
    }


    @ApiOperation(value = "Change status of project", notes = "updates project in database")
    @ApiParam(value = "Project details", required = true)
    @ApiResponses({
            @ApiResponse(code = 200, message = "If project was successfully updated"),
            @ApiResponse(code = 400, message = "If user provided user without TEACHER authority or project settings are incorrect"
                    , response = ErrorDto.class),
            @ApiResponse(code = 404, message = "If provided teacher was not found", response = ErrorDto.class)
    })
    @PreAuthorize("hasAuthority('EMPLOYER')")
    @PostMapping(path = "/project/{id}/changeStatus")
    ProjectDto changeStatus(@PathVariable Long id,
                                @RequestParam ProjectStatus status) {
        Project updated = projectService.changeStatus(id, status);
        return projectMapper.toDto(updated);
    }


    @ApiOperation(value = "Search for project with given id")
    @ApiResponses({
            @ApiResponse(code = 200, message = "When project found"),
            @ApiResponse(code = 404, message = "When project not found")
    })
    @GetMapping(path = "/project/{id}")
    ProjectDto findProject(@PathVariable Long id) {
        Project project = projectService.findProject(id).orElseThrow(ProjectNotFoundException::new);
        return projectMapper.toDto(project);
    }

}
