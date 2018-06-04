package edu.pw.eiti.pik.base.elasticsearch;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


public abstract class DataLoader<T, ID extends Serializable> {
	JpaRepository<T, ID> jpaRepository;
	ElasticsearchRepository<T, ID> esRepository;
	
	public DataLoader(JpaRepository<T, ID> jpaRepository, ElasticsearchRepository<T, ID> esRepository) {
		this.jpaRepository = jpaRepository;
		this.esRepository = esRepository;
	}
	
	@PostConstruct
	@Transactional
	public void populate() {
		List<T> values = jpaRepository.findAll();
		esRepository.saveAll(values);
	}
}
