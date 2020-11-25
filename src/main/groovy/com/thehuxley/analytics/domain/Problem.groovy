package com.thehuxley.analytics.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Problem {

    @Id
    String sid
    Long id
    String name
    Integer nd
    List<Object> topics

}
