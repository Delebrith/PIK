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

    public static final String TEACHER = "TEACHER";
    public static final String STUDENT = "STUDENT";
    public static final String THIRD_PARTY = "THIRD_PARTY";
    public static final String EMPLOYER = "EMPLOYER";
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
        if (authorities.contains("ROLE_ANONYMOUS")) {
            participationService.deleteParticipation("student1@mail.com", 4L, false);
            return;
        }
        if ("resign".equals(status)) {
            if (authorities.contains(STUDENT) || authorities.contains(EMPLOYER)
                    || authorities.contains(THIRD_PARTY))
                participationService.deleteParticipation(authUsername, projectId, false);
            else if (authorities.contains(TEACHER))
                participationService.deleteParticipation(authUsername, projectId, true);
        } else if ("sign_up".equals(status)) {
            if (authorities.contains(STUDENT))
                participationService.addParticipation(authUsername, projectId);
        } else if ("accept_participant".equals(status)) {
            if (authorities.contains(TEACHER) || authorities.contains(EMPLOYER)
                    || authorities.contains(THIRD_PARTY))
                participationService.acceptStudent(authUsername, username, projectId);
        } else if ("invite_teacher".equals(status)) {
            if ((authorities.contains(TEACHER) || authorities.contains(EMPLOYER)
                    || authorities.contains(THIRD_PARTY)) && username != null) {
                participationService.inviteUser(authUsername, username, projectId, true);
            }
        } else if ("invite_student".equals(status)) {
            if ((authorities.contains(TEACHER) || authorities.contains(EMPLOYER)
                    || authorities.contains(THIRD_PARTY)) && username != null)
                participationService.inviteUser(authUsername, username, projectId, false);
        } else if ("reject_invitation".equals(status)) {
            if (authorities.contains(TEACHER))
                participationService.deleteParticipation(authUsername, projectId, true);
            else if (authorities.contains(STUDENT) || authorities.contains(EMPLOYER)
                    || authorities.contains(THIRD_PARTY))
                participationService.deleteParticipation(authUsername, projectId, false);
        } else if ("accept_invitation".equals(status)) {
            if (authorities.contains(STUDENT))
                participationService.acceptInvitation(authUsername, projectId, false);
            else if (authorities.contains(TEACHER))
                participationService.acceptInvitation(authUsername, projectId, true);
        }
    }

    // resign, sign_up, accept_participant, invite_teacher, invite_student, reject_invitation

}
