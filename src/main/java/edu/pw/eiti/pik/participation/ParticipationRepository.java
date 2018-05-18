package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Collection<Participation> findByProject(Project project);
}