package edu.pw.eiti.pik.user;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("Authority")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorityDto {
    private Long id;
    private Authorities name;
    private String displayName;

}