package com.thehuxley.analytics.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document
class User {

    enum Role {
        STUDENT,
        TEACHER_ASSISTANT,
        TEACHER,
        ADMIN_INST,
        ADMIN
    }

    @Id
    String sid
    Long id
    String name
    String avatar

}
