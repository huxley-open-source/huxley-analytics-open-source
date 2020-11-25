package com.thehuxley.analytics.repository.mongodb

import com.thehuxley.analytics.domain.Submission

interface SubmissionRepositoryCustom {

    Submission getById(Long id)
    def mapReduce()

}