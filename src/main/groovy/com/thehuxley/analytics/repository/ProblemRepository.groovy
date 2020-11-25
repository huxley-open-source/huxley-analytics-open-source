package com.thehuxley.analytics.repository

import com.thehuxley.analytics.domain.Problem
import org.springframework.data.mongodb.repository.MongoRepository


interface ProblemRepository extends MongoRepository<Problem, String> {

    Problem findById(Long id)

}