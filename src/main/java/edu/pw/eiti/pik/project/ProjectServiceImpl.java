package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.base.event.ProjectCreationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
