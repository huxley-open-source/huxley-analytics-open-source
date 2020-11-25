package com.thehuxley.analytics.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document
class Statistic {
    enum Entity {
        USER,
        PROBLEM,
        GROUP,
        QUIZ
    }

    @Id
    String sid
    Long id
    Entity entity
    Map statistics
    List<Long> users
    List<Long> problems
    Date startDate
    Date endDate
    Date lasUpdated

}
