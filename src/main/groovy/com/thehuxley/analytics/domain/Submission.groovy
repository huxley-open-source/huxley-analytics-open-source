package com.thehuxley.analytics.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Submission {

    enum Evaluation {
        CORRECT,
        WRONG_ANSWER,
        RUNTIME_ERROR,
        COMPILATION_ERROR,
        EMPTY_ANSWER,
        TIME_LIMIT_EXCEEDED,
        WAITING,
        EMPTY_TEST_CASE,
        WRONG_FILE_NAME,
        PRESENTATION_ERROR,
        HUXLEY_ERROR
    }


    @Id
    String sid
    Long id
    Date submissionDate
    Double time
    Integer tries
    Evaluation evaluation
    Long problemId
    Long userId
    Long languageId

}
