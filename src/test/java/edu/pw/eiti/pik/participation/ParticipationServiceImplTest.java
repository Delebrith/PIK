package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.AddProjectToParticipationEvent;
import edu.pw.eiti.pik.base.event.CheckParticipantsAfterDeletedEvent;
import edu.pw.eiti.pik.base.event.ProjectCreationEvent;
import edu.pw.eiti.pik.project.Project;
import edu.pw.eiti.pik.user.Authorities;
import edu.pw.eiti.pik.user.Authority;
import edu.pw.eiti.pik.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@RunWith(MockitoJUnitRunner.class)
@AutoConfigureDataJpa
public class ParticipationServiceImplTest {

    @Mock
    ParticipationRepository participationRepository;

    @InjectMocks
    private ParticipationServiceImpl participationService;

    @Mock
    ApplicationEventPublisher publisher;

    private User teacher;
    private User student;
    private User employer;

    private Participation ownerParticipation;
    private Participation managerParticipation;
    private Participation pendingInviteParticipation;
    private Participation participantParticipation;
    private Participation waitingParticipation;

    private Project project;



    @Before
    public void init() {
        Authority teacherAuth = Authority.builder().name(Authorities.TEACHER).build();
        Authority studentAuth = Authority.builder().name(Authorities.STUDENT).build();
        Authority employerAuth = Authority.builder().name(Authorities.EMPLOYER).build();
        Authority thirdPartyAuth = Authority.builder().name(Authorities.THIRD_PARTY).build();

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        teacher = User.builder()
                .authorities(Arrays.asList(teacherAuth, employerAuth))
                .email("teacher@mail.com")
                .password(passwordEncoder.encode("teacher"))
                .build();

        student = User.builder()
                .authorities(Collections.singletonList(studentAuth))
                .email("student@mail.com")
                .password(passwordEncoder.encode("student"))
                .build();

        employer = User.builder()
                .authorities(Arrays.asList(thirdPartyAuth, employerAuth))
                .email("thirdparty@mail.com")
                .password(passwordEncoder.encode("thirdparty"))
                .build();

        project = Project.builder()
                .id(1L)
                .description("desc")
                .name("name")
                .numberOfParticipants(1)
                .build();

        ownerParticipation = Participation.builder()
                .project(project)
                .status(ParticipationStatus.OWNER)
                .user(employer)
                .build();

        managerParticipation = Participation.builder()
                .project(project)
                .status(ParticipationStatus.MANAGER)
                .user(teacher)
                .build();

        pendingInviteParticipation = Participation.builder()
                .project(project)
                .status(ParticipationStatus.PENDING_INVITATION)
                .user(teacher)
                .build();

        participantParticipation = Participation.builder()
                .project(project)
                .status(ParticipationStatus.PARTICIPANT)
                .user(student)
                .build();

        waitingParticipation = Participation.builder()
                .project(project)
                .status(ParticipationStatus.WAITING_FOR_ACCEPTANCE)
                .user(student)
                .build();
    }

    @Test
    @WithMockUser(username = "student@mail.com", authorities = "STUDENT")
    public void deleteStudentParticipant() {
        when(participationRepository.findByUser_EmailAndProject_Id(student.getUsername(), project.getId())).thenReturn(participantParticipation);
        participationService.deleteParticipation(student.getUsername(), project.getId(), false);
        verify(participationRepository, times(1)).delete(participantParticipation);
        verify(publisher, times(1)).publishEvent(new CheckParticipantsAfterDeletedEvent(project.getId(), false));
    }

    @Test
    @WithMockUser(username = "student@mail.com", authorities = "TEACHER")
    public void deleteTeacherParticipant() {
        when(participationRepository.findByUser_EmailAndProject_Id(teacher.getUsername(), project.getId())).thenReturn(managerParticipation);
        participationService.deleteParticipation(teacher.getUsername(), project.getId(), true);
        verify(participationRepository, times(1)).delete(managerParticipation);
        verify(publisher, times(1)).publishEvent(new CheckParticipantsAfterDeletedEvent(project.getId(), true));
    }

    @Test
    @WithMockUser
    public void signUp() {
        participationService.addParticipation(student.getUsername(), project.getId());
        Participation participation = Participation.builder()
                .status(ParticipationStatus.WAITING_FOR_ACCEPTANCE)
                .build();
        verify(publisher, times(1)).publishEvent(new AddProjectToParticipationEvent(participation, student.getUsername(), project.getId()));
    }

}
