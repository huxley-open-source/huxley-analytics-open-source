package com.thehuxley.analytics.repository

import com.thehuxley.analytics.domain.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository  extends MongoRepository<User, String> {

    User findById(Long id)

}