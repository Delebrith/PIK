package edu.pw.eiti.pik.participation;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import edu.pw.eiti.pik.project.ProjectStatus;


@RestController("/participation")
public class ParticipationController {

    private final ParticipationService participationService;
    private final ParticipationMapper participationMapper = ParticipationMapper.getInstance();

    @Autowired
    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(ParticipationStatus.class, new ParticipationStatusConverter());
    }

    @ApiOperation(value = "Change participation status",
            notes = "Allow current user to change participation status in current project")
    @PostMapping(path = "/participation/change")
    @ApiResponses(
            @ApiResponse(code = 200, message = "When status was successfully changed")
    )
    void changeStatus(@RequestBody ParticipationChangeDto participationChange) {
        participationService.changeStatus(participationChange.getStatus(),
                participationChange.getProjectId(),
                participationChange.getUsername());
    }


    @ApiOperation(value = "Search for currently authentcated user's projects projects")
    @ApiResponses({@ApiResponse(code = 200, message = "If user is in project")})
    @GetMapping(path = "/participation/project/{projectId}/user/{username}")
    List<ParticipationDto> getUserParticipationStatus(@PathVariable Long projectId,
            						@PathVariable String username,
                            		@RequestParam(name="status", required=false) List<ProjectStatus> statuses) {

    	 return participationService.findByUser_EmailAndProject_Id(username, projectId)
    			 .stream().map(participationMapper::toDto).collect(Collectors.toList());
    }
}
