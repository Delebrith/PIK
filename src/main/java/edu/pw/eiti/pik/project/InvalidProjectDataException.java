package edu.pw.eiti.pik.project;

public class InvalidProjectDataException extends RuntimeException {

    public InvalidProjectDataException() {
        super("Project settings are invalid.");
    }
}
