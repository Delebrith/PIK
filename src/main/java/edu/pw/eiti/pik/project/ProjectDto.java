package edu.pw.eiti.pik.project;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@ApiModel("Project")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {

    private String id;    
    private String name;
    private String description;
    private Boolean isGraduateWork = false;
    @Min(1)
    private Integer numberOfParticipants;
    private ProjectStatus status = ProjectStatus.CREATED;
    @Min(0)
    private Integer ects = 0;
    @Min(0)
    private Integer minimumPay = 0;
    @Min(0)
    private Integer maximumPay = 0;
}
