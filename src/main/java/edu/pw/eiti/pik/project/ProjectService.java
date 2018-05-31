package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.base.event.AddProjectToESEvent;
import edu.pw.eiti.pik.base.event.AddProjectToParticipationEvent;
import edu.pw.eiti.pik.base.event.CancelProjectEvent;
import edu.pw.eiti.pik.base.event.CheckParticipantsAfterDeletedEvent;
import edu.pw.eiti.pik.base.event.CheckProjectStatusEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {
    Project getProjectInfo();
    List<Project> getProjects();
    List<Project> getWaitingForStudentsProjects();
    List<Project> getReportedProjects();
    void createProject(Project project, String teacherMail);
    @EventListener
    void checkParticipantsCount(CheckParticipantsAfterDeletedEvent event);
    @EventListener
    void addProjectToParticipation(AddProjectToParticipationEvent event);
    @EventListener
    void cancelProject(CancelProjectEvent event);
    void deleteProject(long projectId);
    void changeStatus(long projectId, ProjectStatus projectStatus);
    void reportProject(long projectId);
    void signUpForProject(long id);
    @EventListener
    void addProjectToES(AddProjectToESEvent event);
	Page<Project> findProjectsByPhraseAndStatus(String phrase, ProjectStatus status, Pageable pageable);
	Page<Project> findProjectsByStatus(ProjectStatus phrase, Pageable pageable);
	@EventListener
    void checkProjectStatus(CheckProjectStatusEvent event);
}
