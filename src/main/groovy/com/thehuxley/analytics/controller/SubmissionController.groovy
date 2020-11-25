package com.thehuxley.analytics.controller

import com.thehuxley.analytics.service.DataService
import com.thehuxley.analytics.service.DatabaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submission")
class SubmissionController {
    @Autowired
    DataService dataService


    @Autowired
    DatabaseService databaseService

    @RequestMapping("populate")
    def populate() {
        databaseService.populate()

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping("updateList")
    def update() {
        databaseService.update()

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.POST)
    def addSubmission(@RequestBody Map submission) {
        dataService.update(submission.submission, false)
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT)
    def updateSubmission(@RequestBody Map submission) {
        dataService.update(submission.submission, true)
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
