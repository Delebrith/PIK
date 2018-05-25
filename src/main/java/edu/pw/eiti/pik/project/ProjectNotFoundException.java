package edu.pw.eiti.pik.project;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException() {
        super("No project found for given credentials");
    }
}
