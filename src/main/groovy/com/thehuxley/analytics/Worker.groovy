package com.thehuxley.analytics

import com.thehuxley.analytics.Util.RestUtils
import com.thehuxley.analytics.domain.Problem
import com.thehuxley.analytics.domain.Submission
import com.thehuxley.analytics.domain.Topic
import com.thehuxley.analytics.domain.User
import com.thehuxley.analytics.domain.Config
import com.thehuxley.analytics.repository.ConfigRepository
import com.thehuxley.analytics.repository.ProblemRepository
import com.thehuxley.analytics.repository.SubmissionRepository
import com.thehuxley.analytics.repository.TopicRepository
import com.thehuxley.analytics.repository.UserRepository
import org.apache.commons.lang.time.StopWatch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Worker {

    static final MAX = 100

    @Autowired SubmissionRepository submissionRepository
    @Autowired ConfigRepository configRepository
    @Autowired ProblemRepository problemRepository
    @Autowired TopicRepository topicRepository
    @Autowired UserRepository userRepository

    def work() {

        println(submissionRepository.getById(117L).evaluation)

        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis()
        def date

        def configs = configRepository.findAll()
        def config

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

            def inc = ++count * MAX

            try {
                RestUtils.getRestClient().get(path: 'submissions', query: [max: MAX, offset: (count * MAX), sort: 'submissionDate', order: 'asc', submissionDateGt: date]) { response, json ->

                    more = !json.empty

                    total = response.headers['total'].value as Long

                    json.each {
                        def submissionInstance = submissionRepository.findById(it.id as Long)
                        Problem problem
                        User user

                        if (!submissionInstance) {
                            submissionInstance = new Submission()
                        }

                        problem = problemRepository.findById(it.problem.id as Long)

                        if (!problem) {
                            problem = new Problem(
                                    id: it.problem.id,
                                    name: it.problem.name,
                                    nd: it.problem.nd,
                                    topics: []
                            )

                            it.problem.topics.each { t ->
                                def topic = topicRepository.findById(t.id as Long)

                                if (!topic) {
                                    topic = topicRepository.save(new Topic(id: t.id, name: t.name))
                                }

                                problem.topics.add(topic)
                            }

                            problemRepository.save(problem)
                        }

                        user = userRepository.findById(it.user.id as Long)

                        if (!user) {
                            user = userRepository.save(
                                    new User(
                                            id: it.user.id,
                                            name: it.user.name,
                                            avatar: it.user.avatar
                                    )
                            )
                        }

                        submissionInstance.id = it.id as Long
                        submissionInstance.evaluation = Submission.Evaluation.valueOf(it.evaluation as String)
                        submissionInstance.problem = problem
                        submissionInstance.submissionDate = formatter.parseDateTime(it.submissionDate as String).toDate()
                        submissionInstance.time = it.time as Double
                        submissionInstance.tries = it.tries as Integer
                        submissionInstance.user = user
                        config.lastQueryDate = submissionInstance.submissionDate

                        submissionRepository.save(submissionInstance)
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

}