package edu.pw.eiti.pik.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, ElasticsearchRepository<User, Long> {
    User findByEmail(String email);
    
    @Query("{ \"bool\": { " +
    		"\"filter\": [" +
    		"{ \"term\": {\"authorities.name\": \"?0\"} }" +
    		"] }," +
    		"\"match\": {" +
    		"\"name\": \"?1\"" +
    		"}}")
    Page<User> findByRoleAndName(String role, String name, Pageable pageable);
}
