package edu.pw.eiti.pik.config.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

@ApiModel("Error")
@Value
public class ErrorDto {

    @ApiModelProperty(required = true, value = "Requested URL", example = "http://localhost:8080/url")
    private String request;

    @ApiModelProperty(required = true, value = "Error summary", example = "Something went wrong")
    private String errorInfo;
}
