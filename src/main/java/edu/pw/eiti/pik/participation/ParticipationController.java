package edu.pw.eiti.pik.participation;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController("/participation")
public class ParticipationController {

    private static final String TEACHER = "TEACHER";
    private static final String STUDENT = "STUDENT";
    private static final String EMPLOYER = "EMPLOYER";

    private final ParticipationService participationService;

    @Autowired
    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @ApiOperation(value = "Change participation status",
            notes = "Allow current user to change participation status in current project")
    @PostMapping(path = "/participation/{status}/{projectId}")
    void changeStatus(@PathVariable String status, @PathVariable Long projectId,
                      @ApiParam(value = "User for whom we want to change status")
                      @RequestBody String username) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        String authUsername = auth.getName();
        if ("resign".equals(status) || "reject_invitation".equals(status)) {
            if (!authorities.contains(TEACHER))
                participationService.deleteParticipation(authUsername, projectId, false);
            else
                participationService.deleteParticipation(authUsername, projectId, true);
        } else if ("sign_up".equals(status)) {
            if (authorities.contains(TEACHER) || authorities.contains(STUDENT))
                participationService.addParticipation(authUsername, projectId);
        } else if ("accept_student".equals(status)) {
            if (authorities.contains(EMPLOYER) && !username.equals("{}"))
                participationService.acceptParticipant(authUsername, username, projectId, false);
        } else if ("accept_teacher".equals(status)) {
            if (authorities.contains(EMPLOYER) && !username.equals("{}"))
                participationService.acceptParticipant(authUsername, username, projectId, true);
        } else if ("invite".equals(status)) {
            if (!authorities.contains(STUDENT) && !username.equals("{}"))
                participationService.inviteUser(authUsername, username, projectId);
        } else if ("accept_invitation".equals(status)) {
            if (authorities.contains(STUDENT))
                participationService.acceptInvitation(authUsername, projectId, false);
            else if (authorities.contains(TEACHER))
                participationService.acceptInvitation(authUsername, projectId, true);
        }
    }


}
