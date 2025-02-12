package com.sparta.taptoon.global.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

  @PersistenceContext
  private EntityManager entityManager;
  
  @Bean
  public JPAQueryFactory paQueryFactory() {
    return new JPAQueryFactory(entityManager);
  }
}

