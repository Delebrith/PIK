package edu.pw.eiti.pik.project.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.pw.eiti.pik.project.Project;
import edu.pw.eiti.pik.project.ProjectStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project findByName(String name);
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    @Query("select p from Project p where p = " +
            "some(select part.project from Participation part where part.user =" +
            "some(select u from user_ u where u.email = :email) )")
    Page<Project> findByUser(@Param("email") String email, Pageable pageable);
}
