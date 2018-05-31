package edu.pw.eiti.pik.base.elasticsearch;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import edu.pw.eiti.pik.user.UserRepository;


public abstract class DataLoader<T, ID extends Serializable> {
	JpaRepository<T, ID> jpaRepository;
	ElasticsearchRepository<T, ID> esRepository;
	
	public DataLoader(JpaRepository<T, ID> jpaRepository, ElasticsearchRepository<T, ID> esRepository) {
		this.jpaRepository = jpaRepository;
		this.esRepository = esRepository;
	}
	
	@PostConstruct
	public void populate() {
		List<T> values = jpaRepository.findAll();
		esRepository.saveAll(values);
	}
}
