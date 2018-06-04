package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.base.event.CancelProjectEvent;
import edu.pw.eiti.pik.base.event.SignUpEvent;
import edu.pw.eiti.pik.participation.Participation;
import edu.pw.eiti.pik.participation.ParticipationStatus;
import edu.pw.eiti.pik.project.db.ProjectRepository;
import edu.pw.eiti.pik.project.es.ProjectESRepository;
import edu.pw.eiti.pik.user.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@RunWith(MockitoJUnitRunner.class)
@AutoConfigureDataJpa
public class ProjectServiceImplTest {

    @Mock
    ProjectRepository projectRepository;
    @Mock
    ProjectESRepository projectESRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @InjectMocks
    ProjectServiceImpl projectService;

    private Project mockProject;
    private Project waitingProject;
    private Project startedProject;
    private User user;
    private User student;
    private Authentication authentication = Mockito.mock(Authentication.class);
    private SecurityContext securityContext = Mockito.mock(SecurityContext.class);

    @Before
    public void init() {
        SecurityContextHolder.setContext(securityContext);
        mockProject = Project.builder().name("Projekt testowy")
                .description("jakiś opis")
                .ects(0)
                .minimumPay(0)
                .maximumPay(0)
                .isGraduateWork(false)
                .numberOfParticipants(1)
                .status(ProjectStatus.CREATED)
                .participations(new ArrayList<>())
                .build();

        waitingProject = Project.builder().name("Project")
                .description("Description")
                .ects(1)
                .maximumPay(1000)
                .minimumPay(500)
                .isGraduateWork(false)
                .numberOfParticipants(2)
                .status(ProjectStatus.WAITING_FOR_STUDENTS)
                .participations(new ArrayList<>())
                .id(213L)
                .build();

        startedProject = Project.builder().name("Project")
                .description("Description")
                .ects(1)
                .maximumPay(1000)
                .minimumPay(500)
                .isGraduateWork(false)
                .numberOfParticipants(1)
                .status(ProjectStatus.STARTED)
                .participations(new ArrayList<>())
                .id(52137L)
                .build();

        user = User.builder()
                .email("user@mail.com")
                .id(1001L)
                .name("User")
                .password("123456")
                .build();

        student = User.builder()
                .email("student@mail.com")
                .id(51488L)
                .name("Student")
                .password("1234567890")
                .build();

        setParticipation(ParticipationStatus.PARTICIPANT, student, startedProject);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(projectRepository.findById(waitingProject.getId())).thenReturn(Optional.of(waitingProject));
        when(projectRepository.findById(startedProject.getId())).thenReturn(Optional.of(startedProject));
    }

    private void setParticipation(ParticipationStatus status, User user, Project project) {
        Participation participation = new Participation(11011L, status, user, project);
        project.getParticipations().add(participation);
        user.getParticipations().add(participation);
    }

    @Test
    public void addProject() {
        String teacherName = "teacherName";
        projectService.createProject(mockProject, teacherName);
//        verify(eventPublisher, times(1)).publishEvent(new FindUserEvent(mockProject, ParticipationStatus.OWNER, null));
//        verify(eventPublisher, times(1)).publishEvent(new FindUserEvent(mockProject, ParticipationStatus.OWNER, teacherName));
    }

    @Test
    public void cancelProject() {
        CancelProjectEvent event = new CancelProjectEvent(waitingProject.getId());
        Optional<Project> optionalProject = Optional.of(waitingProject);
        projectService.cancelProject(event);
        verify(projectRepository, times(1)).save(optionalProject.get());
        verify(projectESRepository, times(1)).save(optionalProject.get());
    }

    @Test
    public void ownerSettingsChange() {
        String newName = "New name";
        String newDescr = "New description";
        Integer numOfParticipants = 3;
        Integer minimumPay = 600;
        Integer maximumPay = 1200;
        setParticipation(ParticipationStatus.OWNER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), newName, newDescr, numOfParticipants, minimumPay, maximumPay,
                null, null);
        Project changedProject = waitingProject;
        changedProject.setName(newName);
        changedProject.setDescription(newDescr);
        changedProject.setNumberOfParticipants(numOfParticipants);
        changedProject.setMinimumPay(minimumPay);
        changedProject.setMaximumPay(maximumPay);
        verify(projectRepository, times(1)).save(changedProject);
        verify(projectESRepository, times(1)).save(changedProject);
    }

    @Test
    public void teacherSettingsChange() {
        String newName = "New name";
        String newDescr = "New description";
        Integer ects = 3;
        Boolean isGraduateWork = true;
        setParticipation(ParticipationStatus.MANAGER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), newName, newDescr, null, null, null,
                ects, isGraduateWork);
        Project changedProject = waitingProject;
        changedProject.setName(newName);
        changedProject.setDescription(newDescr);
        changedProject.setEcts(ects);
        changedProject.setIsGraduateWork(isGraduateWork);
        verify(projectRepository, times(1)).save(changedProject);
        verify(projectESRepository, times(1)).save(changedProject);
    }

    @Test(expected = InsufficientAuthorizationException.class)
    public void ownerCannotChangeECTSSetting() {
        Participation participation = new Participation(11011L, ParticipationStatus.OWNER, user, waitingProject);
        waitingProject.getParticipations().add(participation);
        user.getParticipations().add(participation);
        projectService.changeSettings(waitingProject.getId(), null, null, null,
                null, null, 3, null);
    }

    @Test(expected = InsufficientAuthorizationException.class)
    public void ownerCannotChangeGraduateWorkSetting() {
        setParticipation(ParticipationStatus.OWNER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), null, null, null,
                null, null, null, true);
    }

    @Test(expected = InsufficientAuthorizationException.class)
    public void teacherCannotChangeNumberOfParticipantsSetting() {
        setParticipation(ParticipationStatus.MANAGER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), null, null, 4,
                null, null, null, true);
    }

    @Test(expected = InsufficientAuthorizationException.class)
    public void teacherCannotChangeMinimumPaySetting() {
        setParticipation(ParticipationStatus.MANAGER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), null, null, null,
                1500, null, null, true);
    }

    @Test(expected = InsufficientAuthorizationException.class)
    public void teacherCannotChangeMaximumPaySetting() {
        setParticipation(ParticipationStatus.MANAGER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), null, null, null,
                null, 3000, null, true);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void wrongNumberOfParticipantsSetting() {
        setParticipation(ParticipationStatus.OWNER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), null, null, -1,
                null, null, null, null);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void wrongMinimumPaySetting() {
        setParticipation(ParticipationStatus.OWNER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), null, null, -1,
                -100, null, null, null);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void minimumPayMoreThanMaximumSetting() {
        setParticipation(ParticipationStatus.OWNER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), null, null, -1,
                100000, null, null, null);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void wrongMaximumPaySetting() {
        setParticipation(ParticipationStatus.OWNER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), null, null, -1,
                null, -100, null, null);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void maximumPayLessThanMinimumSetting() {
        setParticipation(ParticipationStatus.OWNER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), null, null, -1,
                null, 1, null, null);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void wrongEctsSetting() {
        setParticipation(ParticipationStatus.MANAGER, user, waitingProject);
        projectService.changeSettings(waitingProject.getId(), null, null, null,
                null, null, -1, true);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void cannotChangeMinimumPaySettingAfterStart() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeSettings(startedProject.getId(), null, null, null,
                1, null, null, null);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void cannotChangeMaximumPaySettingAfterStart() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeSettings(startedProject.getId(), null, null, null,
                null, 10000, null, null);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void cannotChangeEctsSettingAfterStart() {
        setParticipation(ParticipationStatus.MANAGER, user, startedProject);
        projectService.changeSettings(startedProject.getId(), null, null, null,
                null, null, 10, null);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void cannotChangeGraduateWordSettingAfterStart() {
        setParticipation(ParticipationStatus.MANAGER, user, startedProject);
        projectService.changeSettings(startedProject.getId(), null, null, null,
                null, null, null, true);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void cannotSetEmptyName() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeSettings(startedProject.getId(), "", null, null,
                null, null, null, null);
    }

    @Test(expected = InvalidProjectSettingsChangeException.class)
    public void cannotSetEmptyDescription() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeSettings(startedProject.getId(), null, "", null,
                null, null, null, null);
    }

    @Test
    public void projectSuspendedWhenNoUsersAndNotStarted() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeSettings(startedProject.getId(), null, null, 10,
                null, null, null, null);
        Project changedProject = startedProject;
        changedProject.setStatus(ProjectStatus.SUSPENDED_MISSING_PARTICIPANTS);
        changedProject.setNumberOfParticipants(10);
        verify(projectRepository, times(1)).save(changedProject);
        verify(projectESRepository, times(1)).save(changedProject);
    }

    @Test
    public void suspendedReportProject() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.SUSPENDED_REPORTED);
        Project changedProject = startedProject;
        changedProject.setStatus(ProjectStatus.SUSPENDED_REPORTED);
        verify(projectRepository, times(1)).save(changedProject);
        verify(projectESRepository, times(1)).save(changedProject);
    }

    @Test
    public void waitForStudentsProject() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        startedProject.setStatus(ProjectStatus.SUSPENDED_MISSING_PARTICIPANTS);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.WAITING_FOR_STUDENTS);
        Project changedProject = startedProject;
        changedProject.setStatus(ProjectStatus.WAITING_FOR_STUDENTS);
        verify(projectRepository, times(1)).save(changedProject);
        verify(projectESRepository, times(1)).save(changedProject);
    }

    @Test
    public void createdProject() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        startedProject.setStatus(ProjectStatus.SUSPENDED_MISSING_TEACHER);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.CREATED);
        Project changedProject = startedProject;
        changedProject.setStatus(ProjectStatus.CREATED);
        verify(projectRepository, times(1)).save(changedProject);
        verify(projectESRepository, times(1)).save(changedProject);
    }

    @Test
    public void startedProject() {
        setParticipation(ParticipationStatus.OWNER, user, waitingProject);
        projectService.changeStatus(waitingProject.getId(), ProjectStatus.STARTED);
        Project changedProject = waitingProject;
        changedProject.setStatus(ProjectStatus.STARTED);
        verify(projectRepository, times(1)).save(changedProject);
        verify(projectESRepository, times(1)).save(changedProject);
    }

    @Test
    public void canceledProject() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.CANCELED);
        Project changedProject = startedProject;
        changedProject.setStatus(ProjectStatus.CANCELED);
        verify(projectRepository, times(1)).save(changedProject);
        verify(projectESRepository, times(1)).save(changedProject);
    }

    @Test
    public void finishedProject() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.FINISHED);
        Project changedProject = startedProject;
        changedProject.setStatus(ProjectStatus.FINISHED);
        verify(projectRepository, times(1)).save(changedProject);
        verify(projectESRepository, times(1)).save(changedProject);
    }

    @Test(expected = InvalidProjectDataException.class)
    public void invalidProjectStatus() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.OVERDUE);
    }

    @Test(expected = InvalidProjectDataException.class)
    public void waitingForStudentsFromWrongStatus() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.WAITING_FOR_STUDENTS);
    }

    @Test(expected = InvalidProjectDataException.class)
    public void createdFromWrongStatus() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.CREATED);
    }

    @Test(expected = InvalidProjectDataException.class)
    public void startedFromWrongStatus() {
        setParticipation(ParticipationStatus.OWNER, user, startedProject);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.STARTED);
    }

    @Test(expected = InvalidProjectDataException.class)
    public void finishedFromWrongStatus() {
        setParticipation(ParticipationStatus.OWNER, user, waitingProject);
        projectService.changeStatus(waitingProject.getId(), ProjectStatus.FINISHED);
    }

    @Test(expected = InsufficientAuthorizationException.class)
    public void createdWithoutAuthStatus() {
        setParticipation(ParticipationStatus.PARTICIPANT, user, startedProject);
        startedProject.setStatus(ProjectStatus.SUSPENDED_MISSING_TEACHER);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.CREATED);
    }

    @Test(expected = InsufficientAuthorizationException.class)
    public void waitingForStudentsWithoutAuthStatus() {
        setParticipation(ParticipationStatus.PARTICIPANT, user, startedProject);
        startedProject.setStatus(ProjectStatus.SUSPENDED_MISSING_PARTICIPANTS);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.WAITING_FOR_STUDENTS);
    }

    @Test(expected = InsufficientAuthorizationException.class)
    public void startedWithoutAuthStatus() {
        setParticipation(ParticipationStatus.PARTICIPANT, user, waitingProject);
        projectService.changeStatus(waitingProject.getId(), ProjectStatus.STARTED);
    }

    @Test(expected = InsufficientAuthorizationException.class)
    public void canceledWithoutAuthStatus() {
        setParticipation(ParticipationStatus.PARTICIPANT, user, startedProject);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.CANCELED);
    }

    @Test(expected = InsufficientAuthorizationException.class)
    public void finishedWithoutAuthStatus() {
        setParticipation(ParticipationStatus.PARTICIPANT, user, startedProject);
        projectService.changeStatus(startedProject.getId(), ProjectStatus.FINISHED);
    }

}
