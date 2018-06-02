package edu.pw.eiti.pik.base.elasticsearch;

import java.util.List;

import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import edu.pw.eiti.pik.project.Project;

@Component
@Profile("develop")
@DependsOn("liquibase")
public class ProjectLoader extends DataLoader<Project, Long> {
	
	public ProjectLoader(JpaRepository<Project, Long> jpaRepository, ElasticsearchRepository<Project, Long> esRepository) {
		super(jpaRepository, esRepository);
	}
}
