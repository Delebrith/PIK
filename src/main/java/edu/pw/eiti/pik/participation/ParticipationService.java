package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.UserAndProjectToParticipationEvent;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.event.EventListener;

public interface ParticipationService {

    void changeStatus(ParticipationStatus status, Long projectId, String username);

    void deleteParticipation(String username, Long projectId);

    void addParticipation(String username, Long projectId);

    void acceptInvitation(String username, Long projectId, Boolean isTeacher);

    void inviteUser(String inviterUsername, String invitedUsername, Long projectId);

    void acceptParticipant(String authUsername, String acceptedUsername, Long projectId, Boolean isTeacher);

    @EventListener
    void addNewParticipation(UserAndProjectToParticipationEvent event);

	List<Participation> findByUser_EmailAndProject_Id(String username, Long projectId);

    List<Participation> findByProject(Long projectId);
}
