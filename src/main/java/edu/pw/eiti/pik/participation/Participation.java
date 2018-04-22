package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.project.Project;
import edu.pw.eiti.pik.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
            name = "users_projects",
            joinColumns = @JoinColumn(
                    name = "participation_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"))
    private List<User> users;

    @ManyToOne(fetch = FetchType.EAGER)
    private Project project;
}
