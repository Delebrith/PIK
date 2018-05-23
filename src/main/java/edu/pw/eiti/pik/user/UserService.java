package edu.pw.eiti.pik.user;

import edu.pw.eiti.pik.base.event.ParticipationForUserCreationEvent;

import java.util.Optional;

interface UserService {

    User getAuthenticatedUser();
    Optional<User> findByEmail(String email);
    String generateToken(User user);
    Optional<User> authenticate(String email, String password);
    void saveUserWIthParticipation(ParticipationForUserCreationEvent event);
}
