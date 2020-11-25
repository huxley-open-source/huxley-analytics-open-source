package com.thehuxley.analytics.controller

import com.thehuxley.analytics.service.DataService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/problem")
class ProblemController {

    @Autowired
    DataService dataService

    @RequestMapping("/{id}")
    def get(@PathVariable(value = "id") Long id, @RequestParam(value = "key", required = false) String key) {
        def tempResponse = dataService.getProblemStatistic(id)
        if (key) {
            tempResponse = [(key): tempResponse[key]]
        }
        return dataService.getResponse(tempResponse)
    }

    @RequestMapping("update/{id}")
    def update(@PathVariable(value = "id") Long id) {
        return dataService.updateProblem(id)
    }


}
