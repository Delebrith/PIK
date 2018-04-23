package edu.pw.eiti.pik.project;

import java.util.List;

public interface ProjectService {
    Project getProjectInfo();
    List<Project> getProjects();
    List<Project> getWaitingForStudentsProjects();
    List<Project> getReportedProjects();
    void createProject(Project project);
    void deleteProject(long projectId);
    void changeStatus(long projectId, ProjectStatus projectStatus);
    void reportProject(long projectId);
    void signUpForProject(long id);
}
