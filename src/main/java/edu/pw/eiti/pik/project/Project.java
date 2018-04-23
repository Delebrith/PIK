package edu.pw.eiti.pik.project;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
    private ProjectStatus status;
}
