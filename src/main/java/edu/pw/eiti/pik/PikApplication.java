package edu.pw.eiti.pik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(
		basePackages = {"edu.pw.eiti.pik.participation", "edu.pw.eiti.pik.user.db", "edu.pw.eiti.pik.project"})
@EnableElasticsearchRepositories(basePackages="edu.pw.eiti.pik.user.es")
public class PikApplication {

	public static void main(String[] args) {
		SpringApplication.run(PikApplication.class, args);
	}
}
