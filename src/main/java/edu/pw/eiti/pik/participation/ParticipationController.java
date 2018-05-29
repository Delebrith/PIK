package edu.pw.eiti.pik.participation;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController("/participation")
public class ParticipationController {

    private final ParticipationService participationService;

    @Autowired
    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @ApiOperation(value = "Change participation status",
            notes = "Allow current user to change participation status in current project")
    @PostMapping(path = "/participation/{status}/{projectId}")
    @ApiResponses(
            @ApiResponse(code = 200, message = "When status was successfully changed")
    )
    void changeStatus(@PathVariable ParticipationChangeStatus status, @PathVariable Long projectId,
                      @ApiParam(value = "User for whom we want to change status")
                      @RequestBody String username) {
        participationService.changeStatus(status, projectId, username);
    }


}
