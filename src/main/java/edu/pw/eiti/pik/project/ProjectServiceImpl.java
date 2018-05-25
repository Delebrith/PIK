package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.base.event.AddProjectToParticipationEvent;
import edu.pw.eiti.pik.base.event.CheckParticipantsAfterDeletedEvent;
import edu.pw.eiti.pik.base.event.ParticipationCreationEvent;
import edu.pw.eiti.pik.base.event.ProjectCreationEvent;
import edu.pw.eiti.pik.participation.Participation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Part;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public Project getProjectInfo() {
        return null;
    }

    @Override
    public List<Project> getProjects() {
        return new ArrayList<>();
    }

    @Override
    public List<Project> getWaitingForStudentsProjects() {
        return new ArrayList<>();
    }

    @Override
    public List<Project> getReportedProjects() {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public void createProject(Project project) {
        Project savedProject = projectRepository.save(project);
        publisher.publishEvent(new ProjectCreationEvent(savedProject));
    }

    @Override
    @EventListener
    public void checkParticipantsCount(CheckParticipantsAfterDeletedEvent event) {
        Optional<Project> project = projectRepository.findById(event.getProjectId());
        if (!project.isPresent())
            throw new ProjectNotFoundException();
        else {
            if (event.getIsTeacher())
                project.get().setStatus(ProjectStatus.SUSPENDED_MISSING_TEACHER);
            else if (project.get().getNumberOfParticipants() > project.get().getParticipations().size())
                project.get().setStatus(ProjectStatus.SUSPENDED_MISSING_PARTICIPANTS);
            projectRepository.save(project.get());
        }
    }

    @Override
    @EventListener
    public void addProjectToParticipation(AddProjectToParticipationEvent event) {
        Participation participation = event.getParticipation();
        Optional<Project> project = projectRepository.findById(event.getProjectId());
        if (project.isPresent()) {
            participation.setProject(project.get());
            publisher.publishEvent(new ParticipationCreationEvent(participation));
        }
    }

    @Override
    public void deleteProject(long projectId) {

    }

    @Override
    public void changeStatus(long projectId, ProjectStatus projectStatus) {

    }

    @Override
    public void reportProject(long projectId) {

    }

    @Override
    public void signUpForProject(long id) {

    }
}
