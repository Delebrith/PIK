package edu.pw.eiti.pik.user.es;

import edu.pw.eiti.pik.user.Authority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import edu.pw.eiti.pik.user.User;

import java.util.List;

public interface UserESRepository extends ElasticsearchRepository<User, Long>{

    
    @Query("{ \"bool\": { \"must\": [" +
    		"{ \"prefix\": {\"name\": \"?0\"} }," +
    		"{ \"nested\": {" +
    			" \"path\":\"authorities\"," +
    			" \"query\":{" +
    			"\"match\": {\"authorities.name\": \"?1\"}" +
    		"}}}" +
    		"]}}")
    Page<User> findByNameAndAuthorityName(String name, String authority, Pageable pageable);

	@Query("{\"bool\" : {" +
				"\"should\" : {" +
					"\"prefix\" : { \"name\" : \"?0\" }" +
				"}" +
			"}}")
	List<User> findByName(String name);

	@Query("{\"bool\" : {" +
			"\"must\" : [" +
			"{ \"prefix\" : { \"name\" : \"?0\" } }," +
			"{ \"nested\" : {" +
				" \"path\":\"authorities\"," +
				" \"query\": {" +
					"\"bool\" : {\"filter\": { \"match\" : {" +
						"\"authorities.name\": \"?1\"} " +
					"} }" +
				"}" +
			"}" +
			"}]" +
			"}}")
	Page<User> findByNameAndAuthorityList(String name, String authorities, Pageable pageable);
}

