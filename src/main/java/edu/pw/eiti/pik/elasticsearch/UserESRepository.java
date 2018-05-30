package edu.pw.eiti.pik.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import edu.pw.eiti.pik.user.User;

public interface UserESRepository extends ElasticsearchRepository<User, Long>{

    
    @Query("{ \"bool\": { \"must\": [" +
    		"{ \"prefix\": {\"name\": \"?0\"} }," +
    		"{ \"nested\": {" +
    			" \"path\":\"authorities\"," +
    			" \"query\":{" +
    			"\"match\": {\"authorities.name\": \"?1\"}" +
    		"}}}" +
    		"]}}")
    public Page<User> findByNameAndAuthorityName(String name, String authority, Pageable pageable);
}
