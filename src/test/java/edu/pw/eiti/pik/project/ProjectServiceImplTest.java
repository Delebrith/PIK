package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.base.event.FindUserEvent;
import edu.pw.eiti.pik.participation.ParticipationStatus;
import edu.pw.eiti.pik.project.db.ProjectRepository;
import edu.pw.eiti.pik.user.UserServiceImplTest;
import edu.pw.eiti.pik.user.db.UserRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ActiveProfiles("test")
@RunWith(MockitoJUnitRunner.class)
@AutoConfigureDataJpa
public class ProjectServiceImplTest {

    @Mock
    ProjectRepository projectRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @InjectMocks
    ProjectServiceImpl projectService;

    private Project mockProject;
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
    }

    @Test
    @WithMockUser(username = "firma@mail.com", authorities = "EMPLOYER")
    public void addProject() {
        projectService.createProject(mockProject, null);
//        verify(eventPublisher, times(1)).publishEvent(new FindUserEvent(mockProject, ParticipationStatus.OWNER, null));
    }
}
