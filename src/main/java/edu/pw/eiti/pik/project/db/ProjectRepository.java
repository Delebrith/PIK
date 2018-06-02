package edu.pw.eiti.pik.project.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.pw.eiti.pik.project.Project;
import edu.pw.eiti.pik.project.ProjectStatus;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project findByName(String name);
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    @Query("select p from Project p where p = " +
            "some(select part.project from Participation part where part.user = " +
            "some(select u from user_ u where u.email = :email) ) and " +
            "p.status in :statuses")
    Page<Project> findByUser(@Param("email") String email, @Param(value="statuses") List<ProjectStatus> statuses, Pageable pageable);

    @Query("Select p from Project p where"
    		+ " p.status in :statuses and"
    		+ " p.ects >= :minEcts and"
    		+ " (p.minimumPay >= :minPay or p.maximumPay >= :minPay) and"
    		+ " (p.isGraduateWork = true or :onlyGraduateWork = false)"
    		)
    Page<Project> findByStatus(@Param(value="statuses") List<ProjectStatus> statuses,
    		@Param(value="minEcts") int minEcts,
    		@Param(value="minPay") int minPay,
    		@Param(value="onlyGraduateWork") boolean onlyGraduateWork, Pageable pageable);
}
