package com.thehuxley.analytics.service

import com.thehuxley.analytics.Util.RestUtils
import com.thehuxley.analytics.domain.Config
import com.thehuxley.analytics.domain.Language
import com.thehuxley.analytics.domain.Problem
import com.thehuxley.analytics.domain.Submission
import com.thehuxley.analytics.domain.Topic
import com.thehuxley.analytics.domain.User
import com.thehuxley.analytics.repository.ConfigRepository
import com.thehuxley.analytics.repository.LanguageRepository
import com.thehuxley.analytics.repository.ProblemRepository
import com.thehuxley.analytics.repository.SubmissionRepository
import com.thehuxley.analytics.repository.TopicRepository
import com.thehuxley.analytics.repository.UserRepository
import groovyx.net.http.HttpResponseException
import org.apache.commons.lang.time.StopWatch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class DatabaseService {

    static final MAX = 100

    @Autowired SubmissionRepository submissionRepository
    @Autowired ConfigRepository configRepository
    @Autowired ProblemRepository problemRepository
    @Autowired TopicRepository topicRepository
    @Autowired UserRepository userRepository
    @Autowired LanguageRepository languageRepository
    @Autowired DataService dataService


    DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis()

    def populate() {
        def date
        def config
        def configs = configRepository.findAll()

        if (!configs.empty) {
            config = configs.first()
        } else {
            config = new Config(lastQueryDate: new Date().parse('yyyy/MM/dd', '2009/01/01'))
        }

        DateTime dt = new DateTime((config.lastQueryDate as Date).getTime())
        date = formatter.print(dt)

        def count = 0
        def more = true
        def total = 0
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        while(more) {

            def inc = count++ * MAX

            try {
                RestUtils.getRestClient().get(path: 'submissions', query: [max: MAX, offset: inc, sort: 'submissionDate', order: 'asc', submissionDateGt: date]) { response, json ->

                    more = !json.empty

                    total = response.headers['total'].value as Long

                    json.each {
                        update(it)
                        config.lastQueryDate = formatter.parseDateTime(it.submissionDate as String).toDate()
                    }
                }
                configRepository.save(config)
                println "$inc/$total (${(((inc/total)*100) as Double).trunc(2)}%) - $stopWatch ($config.lastQueryDate)"
            } catch (e) {
                println e.message
                e.finalize()
            }
        }

        stopWatch.stop()
    }

    def update() {

        def lastSubmission

        RestUtils.getRestClient().get(path: 'submissions', query: [max: 1]) { response, json -> lastSubmission = json.first() }

        if (lastSubmission) {
            (lastSubmission.id..1).each {
                if (!submissionRepository.getById(it)) {
                    print "---> Não achou #$it"
                    try {
                        RestUtils.getRestClient().get(path: "submissions/$it") { response, json ->
                            println " - Adicionado."
                            update(json)
                        }
                    } catch (HttpResponseException e) {
                        println " - Algo errado. $e.message"
                    }
                } else {
                    println "OK #$it"
                }
            }
        }
    }

    def update(json) {
        def submissionInstance = submissionRepository.findById(json.id as Long)
        Problem problem
        User user
        Language language

        if (!submissionInstance) {
            submissionInstance = new Submission()
        }

        problem = problemRepository.findById(json.problem.id as Long)

        if (!problem) {
            problem = new Problem(
                    id: json.problem.id,
                    name: json.problem.name,
                    nd: json.problem.nd,
                    topics: []
            )

            json.problem.topics.each { t ->
                def topic = topicRepository.findById(t.id as Long)

                if (!topic) {
                    topic = topicRepository.save(new Topic(id: t.id, name: t.name))
                }

                problem.topics.add(topic)
            }

            problemRepository.save(problem)
        }

        user = userRepository.findById(json.user.id as Long)

        if (!user) {
            user = userRepository.save(
                    new User(
                            id: json.user.id,
                            name: json.user.name,
                            avatar: json.user.avatar
                    )
            )
        }

        language = languageRepository.findById(json.language.id as Long)

        if (!language) {
            language = languageRepository.save(
                    new Language(
                            id: json.language.id,
                            name: json.language.name,
                    )
            )
        }

        submissionInstance.id = json.id as Long
        submissionInstance.evaluation = Submission.Evaluation.valueOf(json.evaluation as String)
        submissionInstance.problemId = problem.id
        submissionInstance.submissionDate = formatter.parseDateTime(json.submissionDate as String).toDate()
        submissionInstance.time = json.time as Double
        submissionInstance.tries = json.tries as Integer
        submissionInstance.userId = user.id
        submissionInstance.languageId = json.language.id

        submissionRepository.save(submissionInstance)
    }

    def mapReduce() {
//        submissionRepository.mapReduce()
        List<Submission> submissionList = submissionRepository.findAll().subList(0, 1000)
        dataService.getResponse(dataService.extractSubmissionsData(submissionList))
//        problemRepository.save(problem)

    }

}
