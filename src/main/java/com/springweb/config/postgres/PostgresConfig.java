package com.springweb.config.postgres;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.springweb.repository.postgres")
public class PostgresConfig {
}
