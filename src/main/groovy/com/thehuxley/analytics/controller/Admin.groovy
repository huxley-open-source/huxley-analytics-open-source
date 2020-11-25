package com.thehuxley.analytics.controller

import com.thehuxley.analytics.service.DatabaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/admin")
class Admin {


    @Autowired
    DatabaseService databaseService

    @RequestMapping("populate")
    def populate() {
        databaseService.populate()

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping("update")
    def update() {
        databaseService.update()

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping("mapreduce")
    def mapReduce() {
        databaseService.mapReduce()
    }
}
