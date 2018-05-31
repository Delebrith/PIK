package edu.pw.eiti.pik.user.db;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.pw.eiti.pik.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
