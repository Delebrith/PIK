package edu.pw.eiti.pik.project;

class ProjectNotFoundException extends RuntimeException {

    ProjectNotFoundException() {
        super("No project found for given arguments");
    }
}
