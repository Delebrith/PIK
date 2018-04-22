package edu.pw.eiti.pik.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("UserCredentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialsDto {

    @ApiModelProperty(required = true, value = "User's e-mail address", example = "student@pw.edu.pl")
    private String email;

    @ApiModelProperty(value = "User's password", example = "my-topsecret-password")
    private String password;
}
