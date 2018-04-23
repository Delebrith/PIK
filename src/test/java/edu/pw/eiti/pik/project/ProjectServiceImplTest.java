package edu.pw.eiti.pik.project;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureDataJpa
public class ProjectServiceImplTest {
    @MockBean
    ProjectRepository projectRepository;

    @Autowired
    ProjectServiceImpl projectService;

    private Project mockProject;

    @Before
    public void init() {
        mockProject.builder().name("Projekt testowy")
                .description("jakiś opis")
                .isPaid(false)
                .isGraduateWork(false)
                .numberOfParticipants(1)
                .status(ProjectStatus.CREATED);
    }

    @Test
    public void addProject(){
        projectService.createProject(mockProject);
        assertEquals(mockProject, projectRepository.findByName("Projekt testowy"));
    }

}
