package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.project.ProjectDto;
import edu.pw.eiti.pik.user.UserDto;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel("Participations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationDto {
    private Long id;
    private ParticipationStatus status;
    private UserDto user;
    private ProjectDto project;
}
