package com.thehuxley.analytics.repository

import com.thehuxley.analytics.domain.Language
import org.springframework.data.mongodb.repository.MongoRepository

interface LanguageRepository extends MongoRepository<Language, String> {

    Language findById(Long id)

}