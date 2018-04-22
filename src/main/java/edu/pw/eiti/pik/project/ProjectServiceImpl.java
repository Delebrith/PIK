package edu.pw.eiti.pik.project;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
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
