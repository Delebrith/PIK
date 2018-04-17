package edu.pw.eiti.pik.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("No user found for given credentials");
    }
}
