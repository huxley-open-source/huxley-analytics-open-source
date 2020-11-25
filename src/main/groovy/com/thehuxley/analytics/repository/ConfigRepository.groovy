package com.thehuxley.analytics.repository;

import com.thehuxley.analytics.domain.Config;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigRepository extends MongoRepository<Config, String> {

}
