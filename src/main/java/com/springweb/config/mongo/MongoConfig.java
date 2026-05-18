package com.springweb.config.mongo;


import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.springweb.repository.mongo")
public class MongoConfig {
}
