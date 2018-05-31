package edu.pw.eiti.pik.project.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.pw.eiti.pik.project.Project;
import edu.pw.eiti.pik.project.ProjectStatus;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project findByName(String name);
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
}
