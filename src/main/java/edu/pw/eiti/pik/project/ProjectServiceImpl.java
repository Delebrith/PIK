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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
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
            publisher.publishEvent(new FindUserEvent(project, ParticipationStatus.PENDING_INVITATION, teacherMail));

    }

    @Override
    @EventListener
    public void addProjectToES(AddProjectToESEvent event) {
        projectESRepository.save(event.getProject());
        checkProjectStatus(new CheckProjectStatusEvent(event.getProject().getId()));
    }

    @Override
    @EventListener
    @Transactional
    public void checkParticipantsCount(CheckParticipantsAfterDeletedEvent event) {
        Optional<Project> project = projectRepository.findById(event.getProjectId());
        if (!project.isPresent())
            throw new ProjectNotFoundException();
        else {
            if (!checkProjectBeforeStart(project.get().getStatus())) {
                if (project.get().getParticipations().stream().noneMatch(p -> p.getStatus().equals(ParticipationStatus.MANAGER)))
                    project.get().setStatus(ProjectStatus.SUSPENDED_MISSING_TEACHER);
                else if (project.get().getNumberOfParticipants() > project.get().getParticipations().stream().filter(p -> p.getStatus().equals(ParticipationStatus.PARTICIPANT)).count())
                    project.get().setStatus(ProjectStatus.SUSPENDED_MISSING_PARTICIPANTS);
            }
            projectRepository.save(project.get());
            projectESRepository.save(project.get());
        }
    }

    @Override
    @EventListener
    public void addProjectToParticipation(AddProjectToParticipationEvent event) {
        Participation participation = event.getParticipation();
        Project project = projectRepository.findById(event.getProjectId()).orElseThrow(ProjectNotFoundException::new);
        participation.setProject(project);
        project.getParticipations().add(participation);
        publisher.publishEvent(new AuthenticatedParticipationCreationEvent(participation));
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
            projectESRepository.save(project.get());
        }
    }

    @Override
    @EventListener
    public void checkProjectStatus(CheckProjectStatusEvent event) {
        Project project = projectRepository.findById(event.getProjectId()).orElseThrow(ProjectNotFoundException::new);
        if (project.getStatus() == ProjectStatus.CREATED && project.getParticipations().stream().anyMatch(p -> p.getStatus() == ParticipationStatus.MANAGER))
            project.setStatus(ProjectStatus.WAITING_FOR_STUDENTS);
        else if ((project.getStatus().equals(ProjectStatus.WAITING_FOR_STUDENTS) || project.getStatus().equals(ProjectStatus.SUSPENDED_MISSING_PARTICIPANTS))
            && project.getNumberOfParticipants() <= project.getParticipations().stream().filter(p -> p.getStatus().equals(ParticipationStatus.PARTICIPANT)).count()) {
            if (project.getParticipations().stream().noneMatch(p -> p.getStatus().equals(ParticipationStatus.MANAGER)))
                project.setStatus(ProjectStatus.SUSPENDED_MISSING_TEACHER);
            else
                project.setStatus(ProjectStatus.STARTED);
        }
        else if (project.getStatus().equals(ProjectStatus.SUSPENDED_MISSING_TEACHER)
                && project.getParticipations().stream().anyMatch(p -> p.getStatus().equals(ParticipationStatus.MANAGER))) {
            if (project.getNumberOfParticipants() > project.getParticipations().stream().filter(p -> p.getStatus().equals(ParticipationStatus.PARTICIPANT)).count())
                project.setStatus(ProjectStatus.SUSPENDED_MISSING_PARTICIPANTS);
            else
                project.setStatus(ProjectStatus.STARTED);
        } else {
            return;
        }
        projectRepository.save(project);
        projectESRepository.save(project);
    }

    @Override
    public void changeSettings(Long projectId, String name, String description, Integer numOfParticipants,
                               Integer minimumPay, Integer maximumPay, Integer ects, Boolean isGraduateWork) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Participation> participations = project.getParticipations().stream().filter(x -> x.getUser().getEmail().equals(username)).collect(Collectors.toList());
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

    @Override
    public Optional<Project> findProject(Long id) {
        return projectRepository.findById(id);
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
    public Project changeStatus(long projectId, ProjectStatus projectStatus) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Participation> participations = project.getParticipations().stream()
                .filter(x -> x.getUser().getEmail().equals(username)).collect(Collectors.toList());

        switch (projectStatus) {
            case SUSPENDED_REPORTED:
                project.setStatus(ProjectStatus.SUSPENDED_REPORTED);
                break;
            case CREATED:
                if (participations.stream().noneMatch(x ->
                    x.getStatus().equals(ParticipationStatus.OWNER) || x.getStatus().equals(ParticipationStatus.MANAGER)))
                        throw new InsufficientAuthorizationException();
                if (project.getStatus() != ProjectStatus.SUSPENDED_MISSING_TEACHER)
                    throw new InvalidProjectDataException();
                project.setStatus(ProjectStatus.CREATED);
                break;
            case WAITING_FOR_STUDENTS:
                if (participations.stream().noneMatch(x ->
                        x.getStatus().equals(ParticipationStatus.OWNER) || x.getStatus().equals(ParticipationStatus.MANAGER)))
                    throw new InsufficientAuthorizationException();
                if (project.getStatus() != ProjectStatus.SUSPENDED_MISSING_PARTICIPANTS)
                    throw new InvalidProjectDataException();
                project.setStatus(ProjectStatus.WAITING_FOR_STUDENTS);
                break;
            case STARTED:
                if (participations.stream().noneMatch(x ->
                        x.getStatus().equals(ParticipationStatus.OWNER) || x.getStatus().equals(ParticipationStatus.MANAGER)))
                    throw new InsufficientAuthorizationException();
                if (project.getStatus() != ProjectStatus.WAITING_FOR_STUDENTS)
                    throw new InvalidProjectDataException();
                project.setStatus(ProjectStatus.STARTED);
                break;
            case CANCELED:
                if (participations.stream().noneMatch(x ->
                        x.getStatus().equals(ParticipationStatus.OWNER) || x.getStatus().equals(ParticipationStatus.MANAGER)))
                    throw new InsufficientAuthorizationException();
                project.setStatus(ProjectStatus.CANCELED);
                break;
            case FINISHED:
                if (participations.stream().noneMatch(x ->
                        x.getStatus().equals(ParticipationStatus.OWNER) || x.getStatus().equals(ParticipationStatus.MANAGER)))
                    throw new InsufficientAuthorizationException();
                if (project.getStatus() != ProjectStatus.STARTED)
                    throw new InvalidProjectDataException();
                project.setStatus(ProjectStatus.FINISHED);
                break;
            default:
                throw new InvalidProjectDataException();
        }
        projectRepository.save(project);
        projectESRepository.save(project);
        return project;
    }

    @Override
    public void reportProject(long projectId) {

    }

    @Override
    public void signUpForProject(long id) {
        Project project = projectRepository.findById(id).orElseThrow(ProjectNotFoundException::new);
        publisher.publishEvent(new SignUpEvent(project));
    }

	@Override
	public Page<Project> findProjectsByPhraseAndStatus(String phrase, List<ProjectStatus> statuses,
			int minEcts, int minPay, boolean onlyGraduateWork,
			Pageable pageable) {

		String statusesString = String.join(" ", statuses.stream().map(ProjectStatus::toString).collect(Collectors.toList()));
		return projectESRepository.findProjectsByPhraseAndStatus(phrase, statusesString, minEcts, minPay, onlyGraduateWork, pageable);
	}

	@Override
	public Page<Project> findProjectsWhereStatusInStatuses(List<ProjectStatus> statuses,
			int minEcts, int minPay, boolean onlyGraduateWork, Pageable pageable) {
		return projectRepository.findByStatus(statuses, minEcts, minPay, onlyGraduateWork, pageable);
	}

    @Override
    public Page<Project> findMyProjects(Integer pageNumber, Integer pageSize, List<ProjectStatus> statuses) {
        final String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName)
                .orElseThrow(IllegalStateException::new);
        return projectRepository.findByUser(email, statuses, PageRequest.of(pageNumber, pageSize));
    }
}
