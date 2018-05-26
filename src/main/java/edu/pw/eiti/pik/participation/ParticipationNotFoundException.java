package edu.pw.eiti.pik.participation;

public class ParticipationNotFoundException extends RuntimeException {

    public ParticipationNotFoundException() {
        super("No user found for given credentials");
    }
}
