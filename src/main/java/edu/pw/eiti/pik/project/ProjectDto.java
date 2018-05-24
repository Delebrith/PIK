package edu.pw.eiti.pik.project;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("Project")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {

    private Long id;
    private String name;
    private String description;
    private Boolean isGraduateWork;
    private Integer numberOfParticipants;
    private ProjectStatus status;
    private Integer ects;
    private Integer minimumPay;
    private Integer maximumPay;
}
