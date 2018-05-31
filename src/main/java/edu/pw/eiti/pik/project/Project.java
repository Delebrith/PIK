package edu.pw.eiti.pik.project;

import edu.pw.eiti.pik.participation.Participation;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
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
    @Column(length = 1023)
    private String description;

    @NotNull
    @Builder.Default
    @Min(0)
    private Integer ects = 0;

    @NotNull
    @Builder.Default
    @Min(0)
    private Integer minimumPay = 0;

    @NotNull
    @Builder.Default
    @Min(0)
    private Integer maximumPay = 0;

    @NotNull
    @Builder.Default
    private Boolean isGraduateWork = false;

    @NotNull
    @Min(1)
    private Integer numberOfParticipants;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.CREATED;

    @OneToMany(mappedBy = "project", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Participation> participations = new ArrayList<>();
}
