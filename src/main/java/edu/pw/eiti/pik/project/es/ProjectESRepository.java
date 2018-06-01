package edu.pw.eiti.pik.project.es;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import edu.pw.eiti.pik.project.Project;

public interface ProjectESRepository extends ElasticsearchRepository<Project, Long> {
	
	@Query("{ \"bool\": {" +

			"\"filter\": [" +
				"{ \"match\": { \"status\": \"?1\" } }," +
				"{ \"range\": { \"ects\": { \"gte\": ?2 } } }," +
				
				"{ \"bool\": { \"should\": [" +
					"{ \"range\": { \"minimumPay\": { \"gte\": ?3 } } }," +
					"{ \"range\": { \"maximumPay\": { \"gte\": ?3 } } }" +
				"]}}," +
			
				"{ \"bool\": { \"should\": [" +
					"{ \"term\": { \"isGraduateWork\": true } }," +
					"{ \"term\": { \"isGraduateWork\": ?4 } }" +
				"]}}" +
			"]," +
			
			"\"must\": { \"multi_match\": {" +
				"\"fields\": [\"name\",\"description\"]," +
				"\"query\": \"?0\"" +
			"}}" +
	
			"}}")
	Page<Project> findProjectsByPhraseAndStatus(
			String phrase, String statuses, int minEcts, int minPay, boolean onlyGraduateWork,
			Pageable pageable);
}
