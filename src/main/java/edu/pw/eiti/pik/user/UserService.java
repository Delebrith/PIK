package edu.pw.eiti.pik.user;

import edu.pw.eiti.pik.base.event.EmailParticipationCreationEvent;
import edu.pw.eiti.pik.base.event.AuthenticatedParticipationCreationEvent;

import java.util.Optional;

import edu.pw.eiti.pik.base.event.FindUserEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

interface UserService {

    User getAuthenticatedUser();
    Optional<User> findByEmail(String email);
    String generateToken(User user);
    Optional<User> authenticate(String email, String password);
	Page<User> findByNameAndAuthorityName(String name, String authority, Pageable pageable);
	void addUser(FindUserEvent event);
}
