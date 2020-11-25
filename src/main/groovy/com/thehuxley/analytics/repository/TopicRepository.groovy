package com.thehuxley.analytics.repository

import com.thehuxley.analytics.domain.Topic
import org.springframework.data.mongodb.repository.MongoRepository

interface TopicRepository extends MongoRepository<Topic, String> {

    Topic findById(Long id)

}