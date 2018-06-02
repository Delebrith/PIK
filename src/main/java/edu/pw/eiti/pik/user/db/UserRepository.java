package edu.pw.eiti.pik.user.db;

import edu.pw.eiti.pik.user.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.pw.eiti.pik.user.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    List<User> findAllDistinctByAuthoritiesIn(List<Authority> authorities);
}
