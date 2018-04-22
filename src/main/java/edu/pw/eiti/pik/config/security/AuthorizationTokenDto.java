package edu.pw.eiti.pik.config.security;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@ApiModel("AuthorizationTokenDto")
@Getter
@AllArgsConstructor
public class AuthorizationTokenDto {

    @ApiModelProperty(required = true, value = "User's authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5...")
    private String token;
}
