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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public void changeSettings(Long projectId, String name, String description, Integer numOfParticipants,
                               Integer minimumPay, Integer maximumPay, Integer ects, Boolean isGraduateWork) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Participation> participations = project.getParticipations().stream().filter(x -> x.getStatus().name().equals(username)).collect(Collectors.toList());
        if (name != null) {
            if (!name.isEmpty())
                project.setName(name);
            else
                throw new InvalidProjectSettingsChangeException();
        }
        if (description != null) {
            if (!description.isEmpty())
                project.setDescription(description);
            else
                throw new InvalidProjectSettingsChangeException();
        }
        if (minimumPay != null) {
            if (participations.stream().noneMatch(x -> x.getStatus().equals(ParticipationStatus.OWNER)))
                throw new InsufficientAuthorizationException();
            else if (!checkMinimumPay(minimumPay, maximumPay, project.getMaximumPay())
                    || !checkProjectBeforeStart(project.getStatus()))
                throw new InvalidProjectSettingsChangeException();
            else
                project.setMinimumPay(minimumPay);
        }
        if (maximumPay != null) {
            if (participations.stream().noneMatch(x -> x.getStatus().equals(ParticipationStatus.OWNER)))
                throw new InsufficientAuthorizationException();
            else if (!checkMaximumPay(maximumPay, minimumPay, project.getMinimumPay())
                    || !checkProjectBeforeStart(project.getStatus()))
                throw new InvalidProjectSettingsChangeException();
            else
                project.setMaximumPay(maximumPay);
        }
        if (ects != null) {
            if (participations.stream().noneMatch(x -> x.getStatus().equals(ParticipationStatus.MANAGER)))
                throw new InsufficientAuthorizationException();
            else if (!checkProjectBeforeStart(project.getStatus()) || ects < 0)
                throw new InvalidProjectSettingsChangeException();
            else
                project.setEcts(ects);
        }
        if (isGraduateWork != null) {
            if (participations.stream().noneMatch(x -> x.getStatus().equals(ParticipationStatus.MANAGER)))
                throw new InsufficientAuthorizationException();
            else if (!checkProjectBeforeStart(project.getStatus()))
                throw new InvalidProjectSettingsChangeException();
            else
                project.setIsGraduateWork(isGraduateWork);
        }
        if (numOfParticipants != null) {
            if (numOfParticipants <= 0)
                throw new InvalidProjectSettingsChangeException();
            else if (participations.stream().noneMatch(x -> x.getStatus().equals(ParticipationStatus.OWNER)))
                throw new InsufficientAuthorizationException();
            else {
                project.setNumberOfParticipants(numOfParticipants);
                if (!checkProjectBeforeStart(project.getStatus())
                        && numOfParticipants > project.getParticipations().stream().filter(x -> x.getStatus().equals(ParticipationStatus.PARTICIPANT)).count())
                    project.setStatus(ProjectStatus.SUSPENDED_MISSING_PARTICIPANTS);
            }
        }
        projectRepository.save(project);
        projectESRepository.save(project);
    }

    private Boolean checkMinimumPay(Integer minimumNew, Integer maximumNew, Integer maximumProject) {
        return minimumNew >= 0
                && (maximumNew != null && maximumNew >= minimumNew) || (maximumNew == null && maximumProject >= minimumNew);
    }

    private Boolean checkMaximumPay(Integer maximumNew, Integer minimumNew, Integer minimumProject) {
        return maximumNew >= 0
                && (minimumNew != null && maximumNew >= minimumNew) || (minimumNew == null && maximumNew >= minimumProject);
    }

    private Boolean checkProjectBeforeStart(ProjectStatus status) {
        return status.equals(ProjectStatus.CREATED) || status.equals(ProjectStatus.WAITING_FOR_STUDENTS);
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
