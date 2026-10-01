package com.fitness.aiservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing // it means the annotation we defined for MongoDB in model it will start work
public class MongoConfig {
}
