package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.participation.Participation;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Boolean isPaid;

    @NotNull
    private Boolean isGraduateWork;

    @NotNull
    private Integer numberOfParticipants;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.CREATED;

    @OneToMany(mappedBy = "project")
    @Builder.Default
    private List<Participation> participations = new ArrayList<>();
}
