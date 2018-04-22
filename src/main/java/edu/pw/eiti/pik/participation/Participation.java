package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.project.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.h2.engine.User;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user",
            joinColumns = @JoinColumn(
                    name = "participation_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"))
    private List<User> users;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "project",
            joinColumns = @JoinColumn(
                    name = "participation_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "project_id", referencedColumnName = "id"))
    private List<Project> projects;
}
