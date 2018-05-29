package edu.pw.eiti.pik.participation;


import edu.pw.eiti.pik.user.Authorities;
import edu.pw.eiti.pik.user.Authority;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    private static final String THIRD_PARTY = "THIRD_PARTY";
    private static final String EMPLOYER = "EMPLOYER";
    private static final String ADMIN = "ADMIN";
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
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        String authUsername = auth.getName();
        if ("resign".equals(status) || "reject_invitation".equals(status)) {
            if (!authorities.contains(ADMIN) && !authorities.contains(TEACHER))
                participationService.deleteParticipation(authUsername, projectId, false);
            else if (!authorities.contains(ADMIN))
                participationService.deleteParticipation(authUsername, projectId, true);
            else
                throw new IllegalStateException();
        } else if ("sign_up".equals(status)) {
            if (authorities.contains(STUDENT))
                participationService.addParticipation(authUsername, projectId);
            else
                throw new IllegalStateException();
        } else if ("accept_participant".equals(status)) {
            if (authorities.contains(EMPLOYER) && !username.equals("{}"))
                participationService.acceptStudent(authUsername, username, projectId);
            else
                throw new IllegalStateException();
        } else if ("invite_teacher".equals(status)) {
            if (!authorities.contains(ADMIN) && !authorities.contains(STUDENT) && !username.equals("{}"))
                participationService.inviteUser(authUsername, username, projectId);
            else
                throw new IllegalStateException();
        } else if ("invite_student".equals(status)) {
            if (!authorities.contains(ADMIN) && !authorities.contains(STUDENT) && !username.equals("{}"))
                participationService.inviteUser(authUsername, username, projectId);
            else
                throw new IllegalStateException();
        } else if ("accept_invitation".equals(status)) {
            if (authorities.contains(STUDENT) && !authorities.contains(ADMIN))
                participationService.acceptInvitation(authUsername, projectId, false);
            else if (authorities.contains(TEACHER) && !authorities.contains(ADMIN))
                participationService.acceptInvitation(authUsername, projectId, true);
            else
                throw new IllegalStateException();
        }
        else
            throw new IllegalStateException();
    }

    // resign, sign_up, accept_participant, invite_teacher, invite_student, reject_invitation

}
