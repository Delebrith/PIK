package edu.pw.eiti.pik.base.config.web;

import edu.pw.eiti.pik.project.InsufficientAuthorizationException;
import edu.pw.eiti.pik.project.InvalidProjectDataException;
import edu.pw.eiti.pik.project.InvalidProjectSettingsChangeException;
import edu.pw.eiti.pik.user.InvalidAuthorityException;
import edu.pw.eiti.pik.user.InvalidUserDataException;
import edu.pw.eiti.pik.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @org.springframework.web.bind.annotation.ExceptionHandler(UserNotFoundException.class)
    @ResponseBody
    ErrorDto handleResourceNotFound(final HttpServletRequest req, final Exception ex) {
        return new ErrorDto(req.getRequestURL().toString(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @org.springframework.web.bind.annotation.ExceptionHandler(InsufficientAuthorizationException.class)
    @ResponseBody
    ErrorDto handleUnauthorized(final HttpServletRequest req, final Exception ex) {
        return new ErrorDto(req.getRequestURL().toString(), ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler({
            InvalidAuthorityException.class, InvalidProjectDataException.class,
            InvalidUserDataException.class})
    @ResponseBody
    ErrorDto handleBadRequest(final HttpServletRequest req, final Exception ex) {
        return new ErrorDto(req.getRequestURL().toString(), ex.getMessage());
    }

}
