package edu.pw.eiti.pik.user;

import edu.pw.eiti.pik.base.event.ManagerParticipationCreationEvent;
import edu.pw.eiti.pik.base.event.OwnerParticipationCreationEvent;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

interface UserService {

    User getAuthenticatedUser();
    Optional<User> findByEmail(String email);
    String generateToken(User user);
    Optional<User> authenticate(String email, String password);
    void saveProjectOwner(OwnerParticipationCreationEvent event);
    void saveProjectManager(ManagerParticipationCreationEvent event);
	Page<User> findByNameAndAuthorityName(String name, String authority, Pageable pageable);
}
