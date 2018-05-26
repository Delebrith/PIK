package edu.pw.eiti.pik.participation;


import edu.pw.eiti.pik.user.Authorities;
import edu.pw.eiti.pik.user.Authority;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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

    @Autowired
    private ParticipationService participationService;

    @ApiOperation(value = "Change participation status",
            notes = "Allow current user to change participation status in current project")
    @PostMapping(path = "/participation/{status}/{projectId}")
    void changeStatus(@PathVariable String status, @PathVariable Long projectId,
                      @ApiParam(value = "User for whom we want to change status")
                      @RequestBody String username) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        List<Authorities> authorities = auth.getAuthorities().stream().map(i -> ((Authority) i).getName()).collect(Collectors.toList());
        String authUsername = auth.getName();
        if ("resign".equals(status)) {
            if (authorities.contains(Authorities.STUDENT) || authorities.contains(Authorities.EMPLOYER)
                    || authorities.contains(Authorities.THIRD_PARTY))
                participationService.deleteParticipation(authUsername, projectId, false);
            else if (authorities.contains(Authorities.TEACHER))
                participationService.deleteParticipation(authUsername, projectId, true);
        } else if ("sign_up".equals(status)) {
            if (authorities.contains(Authorities.STUDENT))
                participationService.addParticipation(authUsername, projectId);
        } else if ("accept_participant".equals(status)) {
            if (authorities.contains(Authorities.TEACHER) || authorities.contains(Authorities.EMPLOYER)
                    || authorities.contains(Authorities.THIRD_PARTY))
                participationService.acceptStudent(authUsername, username, projectId);
        } else if ("invite_teacher".equals(status)) {
            if ((authorities.contains(Authorities.TEACHER) || authorities.contains(Authorities.EMPLOYER)
                    || authorities.contains(Authorities.THIRD_PARTY)) && username != null) {
                participationService.inviteUser(authUsername, username, projectId, true);
            }
        } else if ("invite_student".equals(status)) {
            if ((authorities.contains(Authorities.TEACHER) || authorities.contains(Authorities.EMPLOYER)
                    || authorities.contains(Authorities.THIRD_PARTY)) && username != null)
                participationService.inviteUser(authUsername, username, projectId, false);
        } else if ("reject_invitation".equals(status)) {
            if (authorities.contains(Authorities.TEACHER))
                participationService.deleteParticipation(authUsername, projectId, true);
            else if (authorities.contains(Authorities.STUDENT) || authorities.contains(Authorities.EMPLOYER)
                    || authorities.contains(Authorities.THIRD_PARTY))
                participationService.deleteParticipation(authUsername, projectId, false);
        } else if ("accept_invitation".equals(status)) {
            if (authorities.contains(Authorities.STUDENT))
                participationService.acceptInvitation(authUsername, projectId, false);
            else if (authorities.contains(Authorities.TEACHER))
                participationService.acceptInvitation(authUsername, projectId, true);
        }
    }

    // resign, sign_up, accept_participant, invite_teacher, invite_student, reject_invitation

}
