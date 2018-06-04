package edu.pw.eiti.pik.project;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@RunWith(MockitoJUnitRunner.class)
@AutoConfigureDataJpa
public class ProjectControllerTest {

    @Mock
    private ProjectService projectService = mock(ProjectServiceImpl.class);

    @InjectMocks
    private ProjectController projectController = new ProjectController(projectService);

    @Test(expected = ProjectNotFoundException.class)
    public void findNonExistingProject() {
        Long testId = 321L;
        assertEquals(projectController.findProject(testId).getId(), testId);
    }
}