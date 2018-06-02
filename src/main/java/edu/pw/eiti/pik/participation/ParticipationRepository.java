package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByProject(Project project);
    List<Participation> findByUser_EmailAndProject_Id(String username, Long projectId);
}