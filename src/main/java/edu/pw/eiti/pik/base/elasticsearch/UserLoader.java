package edu.pw.eiti.pik.base.elasticsearch;

import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import edu.pw.eiti.pik.user.User;

@Component
@Profile("develop")
@DependsOn("liquibase")
public class UserLoader extends DataLoader<User, Long> {
	
	public UserLoader(JpaRepository<User, Long> jpaRepository, ElasticsearchRepository<User, Long> esRepository) {
		super(jpaRepository, esRepository);
	}
}
