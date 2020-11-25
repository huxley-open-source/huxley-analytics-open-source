package com.thehuxley.analytics.repository.mongodb

import static org.springframework.data.mongodb.core.query.Criteria.*

import com.thehuxley.analytics.domain.Submission
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query

class SubmissionRepositoryImpl implements SubmissionRepositoryCustom {

    final MongoOperations operations

    @Autowired
    SubmissionRepositoryImpl(MongoOperations operations) {
        this.operations = operations
    }

    Submission getById(Long id) {
        operations.findOne(new Query(where("id").is(id)), Submission)
    }

    def mapReduce() {
        def results = operations.mapReduce(
                "submission",
                "function() { emit(this.evaluation, 1); }",
                "function(key, values) { return Array.sum(values); }",
                Map
        )

        results.each {
            println it
        }

        return results
    }

}
