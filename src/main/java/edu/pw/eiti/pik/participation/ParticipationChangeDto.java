package edu.pw.eiti.pik.participation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
class ParticipationChangeDto {

    private String username = null;
    @NotNull
    private ParticipationStatus status;
    @NotNull
    private Long projectId;
}
