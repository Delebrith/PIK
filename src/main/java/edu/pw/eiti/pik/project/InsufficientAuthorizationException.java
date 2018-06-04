package edu.pw.eiti.pik.project;

public class InsufficientAuthorizationException extends RuntimeException {

    public InsufficientAuthorizationException() {
        super("Unauthorized to perform this operation");
    }
}
