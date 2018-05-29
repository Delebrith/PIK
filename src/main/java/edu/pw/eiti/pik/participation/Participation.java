package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.project.Project;
import edu.pw.eiti.pik.user.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private Project project;
}
