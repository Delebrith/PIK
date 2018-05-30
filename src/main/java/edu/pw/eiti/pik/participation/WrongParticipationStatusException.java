package edu.pw.eiti.pik.participation;

public class WrongParticipationStatusException extends RuntimeException {

    public WrongParticipationStatusException() { super("Wrong participation status to perform this operation"); }
}
