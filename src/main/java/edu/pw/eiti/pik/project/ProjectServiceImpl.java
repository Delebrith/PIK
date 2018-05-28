package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.base.event.*;
import edu.pw.eiti.pik.participation.Participation;
import edu.pw.eiti.pik.participation.ParticipationStatus;
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

    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ApplicationEventPublisher publisher) {
        this.projectRepository = projectRepository;
        this.publisher = publisher;
    }

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
        publisher.publishEvent(new ProjectCreationEvent(project));
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
            else if (project.get().getNumberOfParticipants() > project.get().getParticipations().stream().filter(p -> p.getStatus().equals(ParticipationStatus.PARTICIPANT)).count())
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
    @EventListener
    public void cancelProject(CancelProjectEvent event) {
        Optional<Project> project = projectRepository.findById(event.getProjectId());
        if (!project.isPresent())
            throw new ProjectNotFoundException();
        else {
            project.get().setStatus(ProjectStatus.CANCELED);
            projectRepository.save(project.get());
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
