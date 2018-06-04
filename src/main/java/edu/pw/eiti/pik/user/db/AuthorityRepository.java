package edu.pw.eiti.pik.user.db;

import edu.pw.eiti.pik.user.Authorities;
import edu.pw.eiti.pik.user.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    List<Authority> findAllByNameIn(List<Authorities> authorities);
}
