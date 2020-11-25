package com.thehuxley.analytics.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Config {

    @Id
    String sid
    Date lastQueryDate

}
