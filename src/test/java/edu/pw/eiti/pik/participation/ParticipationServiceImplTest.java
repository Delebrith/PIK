package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.AddProjectToParticipationEvent;
import edu.pw.eiti.pik.base.event.CheckParticipantsAfterDeletedEvent;
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
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@RunWith(MockitoJUnitRunner.class)
@AutoConfigureDataJpa
public class ParticipationServiceImplTest {

    private SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    private Authentication authentication = Mockito.mock(Authentication.class);

    @Mock
    private ParticipationRepository participationRepository;

    @InjectMocks
    private ParticipationServiceImpl participationService;

    @Mock
    private ApplicationEventPublisher publisher;

    private User teacher;
    private User student;
    private User employer;

    private Participation ownerParticipation;
    private Participation managerParticipation;
    private Participation pendingInviteParticipation;
    private List<Participation> participantParticipation;
    private Participation waitingParticipation;

    private Project project;



    @Before
    public void init() {

        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);

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

        participantParticipation = Arrays.asList(Participation.builder()
                .project(project)
                .status(ParticipationStatus.PARTICIPANT)
                .user(student)
                .build());

        waitingParticipation = Participation.builder()
                .project(project)
                .status(ParticipationStatus.WAITING_FOR_ACCEPTANCE)
                .user(student)
                .build();
    }

    @Test
    public void deleteStudentParticipant() {
        List auths = new ArrayList<SimpleGrantedAuthority>();
        auths.add(new SimpleGrantedAuthority("STUDENT"));
        when(authentication.getName()).thenReturn(student.getUsername());
        when(participationRepository.findByUser_EmailAndProject_Id(student.getUsername(), project.getId())).thenReturn(participantParticipation);
        when(authentication.getAuthorities()).thenReturn(auths);
        ParticipationChangeDto dto = new ParticipationChangeDto();
        dto.setStatus(ParticipationStatus.RESIGNED);
        dto.setProjectId(project.getId());
        participationService.changeStatus(dto.getStatus(), dto.getProjectId(), dto.getUsername());
        verify(participationRepository, times(1)).delete(participantParticipation.get(0));
        verify(publisher, times(1)).publishEvent(new CheckParticipantsAfterDeletedEvent(project.getId(), false));
    }

    @Test
    public void deleteTeacherParticipant() {
        List auths = new ArrayList<SimpleGrantedAuthority>();
        auths.add(new SimpleGrantedAuthority("TEACHER"));
        auths.add(new SimpleGrantedAuthority("EMPLOYER"));
        when(authentication.getName()).thenReturn(teacher.getUsername());
        when(participationRepository.findByUser_EmailAndProject_Id(teacher.getUsername(), project.getId())).thenReturn(Arrays.asList(managerParticipation));
        when(authentication.getAuthorities()).thenReturn(auths);
        ParticipationChangeDto dto = new ParticipationChangeDto();
        dto.setStatus(ParticipationStatus.RESIGNED);
        dto.setProjectId(project.getId());
        participationService.changeStatus(dto.getStatus(), dto.getProjectId(), dto.getUsername());
        verify(participationRepository, times(1)).delete(managerParticipation);
        verify(publisher, times(1)).publishEvent(new CheckParticipantsAfterDeletedEvent(project.getId(), true));
    }

    @Test
    @WithMockUser
    public void signUp() {
        List auths = new ArrayList<SimpleGrantedAuthority>();
        auths.add(new SimpleGrantedAuthority("STUDENT"));
        when(authentication.getName()).thenReturn(student.getUsername());
        when(authentication.getAuthorities()).thenReturn(auths);
        ParticipationChangeDto dto = new ParticipationChangeDto();
        dto.setStatus(ParticipationStatus.WAITING_FOR_ACCEPTANCE);
        dto.setProjectId(project.getId());
        participationService.changeStatus(dto.getStatus(), dto.getProjectId(), dto.getUsername());
        Participation participation = Participation.builder()
                .status(ParticipationStatus.WAITING_FOR_ACCEPTANCE)
                .build();
        verify(publisher, times(1)).publishEvent(new AddProjectToParticipationEvent(participation, student.getUsername(), project.getId()));
    }

}
