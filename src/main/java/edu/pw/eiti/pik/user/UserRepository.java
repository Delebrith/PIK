package edu.pw.eiti.pik.user;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
