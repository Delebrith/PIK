package edu.pw.eiti.pik.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;
    @Override
    public Project getProjectInfo() {
        return null;
    }

    @Override
    public List<Project> getProjects() {
        return null;
    }

    @Override
    public List<Project> getWaitingForStudentsProjects() {
        return null;
    }

    @Override
    public List<Project> getReportedProjects() {
        return null;
    }

    @Override
    public void createProject(Project project) {
        projectRepository.save(project);
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