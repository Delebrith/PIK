package edu.pw.eiti.pik.participation;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;


@RestController("/participation")
public class ParticipationController {

    private final ParticipationService participationService;

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
    @PostMapping(path = "/participation/{status}/{projectId}")
    @ApiResponses(
            @ApiResponse(code = 200, message = "When status was successfully changed")
    )
    void changeStatus(@PathVariable ParticipationStatus status, @PathVariable Long projectId,
                      @ApiParam(value = "User for whom we want to change status")
                      @RequestBody String username) {
        participationService.changeStatus(status, projectId, username);
    }


}
