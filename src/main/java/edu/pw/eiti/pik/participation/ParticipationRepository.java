package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByProject(Project project);
    List<Participation> findByUser_EmailAndProject_Id(String username, Long projectId);

    @Query("select part from Participation as part where part.project =" +
            "(select project from Project as project where project.id = :projectId)")
    List<Participation> findByProjectId(@Param("projectId") Long projectId);
}