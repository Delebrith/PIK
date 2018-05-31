package edu.pw.eiti.pik.project.es;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import edu.pw.eiti.pik.project.Project;
import edu.pw.eiti.pik.project.ProjectStatus;

public interface ProjectESRepository extends ElasticsearchRepository<Project, Long> {
	
	@Query("{ \"bool\":" +
			"{ \"must\":" +
			"{ \"match\": { \"status\": \"?1\" } }," +
			"\"should\": [" +
			"{ \"match\": { \"name\": \"?0\" } }," +
			"{ \"match\": { \"description\": \"?0\" } }" +
			"]," +
			"\"minimum_should_match\": 1" +
			"}}")
	Page<Project> findProjectsByPhraseAndStatus(String phrase, ProjectStatus status, Pageable pageable);
}
