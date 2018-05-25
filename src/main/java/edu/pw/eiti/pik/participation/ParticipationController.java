package edu.pw.eiti.pik.participation;


import edu.pw.eiti.pik.user.Authorities;
import edu.pw.eiti.pik.user.Authority;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController("/participation")
public class ParticipationController {

    @Autowired
    private ParticipationService participationService;

    @ApiOperation(value = "Change participation status",
            notes = "Allow current user to change participation status in current project")
    @PostMapping(path = "/participation/{status}/{projectId}")
    void changeStatus(@PathVariable String status, @PathVariable Long projectId,
                      @ApiParam(value = "User for whom we want to change status")@RequestBody String username) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        Collection<Authority> authorities = (Collection<Authority>) auth.getAuthorities();
        Authority authority = null;
        String authUsername = auth.getName();
        if (authorities.iterator().hasNext())
            authority = authorities.iterator().next();
        else
            return;
        if (authority.getName() == Authorities.ADMIN) {
            return;
        } else if (authority.getName() == Authorities.STUDENT) {
            if (status.equals("resign"))
                participationService.deleteParticipation(authUsername, projectId, false);
            else if (status.equals("signup"))
                participationService.addParticipation(authUsername, projectId, false);
        } else if (authority.getName() == Authorities.EMPLOYER) {
            // accept_participant
            // resign (cancel project?)
            // invite teacher
        } else if (authority.getName() == Authorities.THIRD_PARTY) {
            // ?
        } else if (authority.getName() == Authorities.TEACHER) {
            if (status.equals("resign"))
                participationService.deleteParticipation(authUsername, projectId, true);
            else if (status.equals("accept_participation"))
                participationService.addParticipation(authUsername, projectId, true);
        } else if (authority.getName() == Authorities.AUTHORITY)
            return;
    }

}
