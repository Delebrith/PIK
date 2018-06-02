package edu.pw.eiti.pik.user;

public class InvalidUserDataException extends RuntimeException {
    public InvalidUserDataException() {
        super("Incorrent attemmpt to modify user's data");
    }
}
