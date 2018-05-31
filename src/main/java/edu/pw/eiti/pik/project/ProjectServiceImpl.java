package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.base.event.*;
import edu.pw.eiti.pik.participation.Participation;
import edu.pw.eiti.pik.participation.ParticipationStatus;
import edu.pw.eiti.pik.project.db.ProjectRepository;
import edu.pw.eiti.pik.project.es.ProjectESRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher publisher;
    private final ProjectESRepository projectESRepository;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository,
            					ProjectESRepository projectESRepository,
            					ApplicationEventPublisher publisher) {
        this.projectRepository = projectRepository;
        this.projectESRepository = projectESRepository;
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
    public void createProject(Project project, String teacherMail) {
        if (project.getId() != null || project.getStatus() != ProjectStatus.CREATED)
            throw new InvalidProjectDataException();
        project.setParticipations(new ArrayList<>());
        publisher.publishEvent(new FindUserEvent(project, ParticipationStatus.OWNER, null));
        if (teacherMail != null)
            publisher.publishEvent(new FindUserEvent(project, ParticipationStatus.MANAGER, teacherMail));
    }

    @Override
    @EventListener
    @Transactional
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
            project.get().getParticipations().add(participation);
            publisher.publishEvent(new AuthenticatedParticipationCreationEvent(participation));
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

	@Override
	public Page<Project> findProjectsByPhraseAndStatus(String phrase, ProjectStatus status, Pageable pageable) {
		return projectESRepository.findProjectsByPhraseAndStatus(phrase, status, pageable);
	}

	@Override
	public Page<Project> findProjectsByStatus(ProjectStatus status, Pageable pageable) {
		return projectRepository.findByStatus(status, pageable);
	}
}
