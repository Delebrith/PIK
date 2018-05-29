package edu.pw.eiti.pik.participation;

class ParticipationNotFoundException extends RuntimeException {

    ParticipationNotFoundException() {
        super("No participation found with given arguments");
    }
}
