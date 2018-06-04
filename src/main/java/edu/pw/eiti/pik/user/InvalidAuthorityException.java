package edu.pw.eiti.pik.user;

public class InvalidAuthorityException extends RuntimeException {

    public InvalidAuthorityException() {
        super("Invalid authority list for requested user. ");
    }
}
