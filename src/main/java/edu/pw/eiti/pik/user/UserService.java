package edu.pw.eiti.pik.user;

import java.util.List;
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
	void addUserToProject(FindUserEvent event);
	Page<User> findByNameAndAuthorityList(String name, List<Authorities> authorities, Pageable pageable);
    Page<User> findByAuthorityList(List<Authorities> authorities, Pageable pageable);
    User createUser(User user);
    Optional<User> findUser(Long userId);
    User updateUser(User user);
    List<Authority> getAuthorities();
}
