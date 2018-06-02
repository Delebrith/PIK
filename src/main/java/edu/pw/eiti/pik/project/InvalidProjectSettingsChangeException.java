package edu.pw.eiti.pik.project;

public class InvalidProjectSettingsChangeException extends RuntimeException {

    public InvalidProjectSettingsChangeException() {
        super("Invalid settings for a change project operation");
    }
}
