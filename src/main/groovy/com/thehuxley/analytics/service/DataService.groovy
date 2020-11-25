package com.thehuxley.analytics.service

import com.mongodb.util.JSON
import com.thehuxley.analytics.Util.RestUtils
import com.thehuxley.analytics.domain.Language
import com.thehuxley.analytics.domain.Problem
import com.thehuxley.analytics.domain.Statistic
import com.thehuxley.analytics.domain.Submission
import com.thehuxley.analytics.domain.Topic
import com.thehuxley.analytics.domain.User
import com.thehuxley.analytics.repository.ConfigRepository
import com.thehuxley.analytics.repository.LanguageRepository
import com.thehuxley.analytics.repository.ProblemRepository
import com.thehuxley.analytics.repository.StatisticRepository
import com.thehuxley.analytics.repository.SubmissionRepository
import com.thehuxley.analytics.repository.TopicRepository
import com.thehuxley.analytics.repository.UserRepository
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DataService {
    DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis()
    Date today = new Date()
    def userLogin = ""
    def passwordLogin = ""
/*
    def calculateUserScore (Questionnaire questionnaire, User user) {
        def problems = QuestionnaireProblem.findAllByQuestionnaire(questionnaire).problem
        def returnData = [:]



        def total = 0
        problems.each { Problem problem ->

            def questionnaireProblem = QuestionnaireProblem.findByQuestionnaireAndProblem(questionnaire, problem)

            def evaluation

            def submission = Submission.findByProblemAndUserAndEvaluationAndSubmissionDateLessThan(
                    problem,
                    user,
                    Submission.Evaluation.CORRECT,
                    questionnaire.endDate
            )

            if (submission) {
                evaluation = submission.evaluation
            } else {
                evaluation = Submission.findByProblemAndUserAndSubmissionDateLessThan(
                        problem,
                        user,
                        questionnaire.endDate,
                        [sort: "submissionDate", order: "desc"]
                )?.evaluation
            }

            def data = [
                    status : evaluation,
                    score  : evaluation == Submission.Evaluation.CORRECT ? questionnaireProblem.score : 0,
                    penalty: QuestionnaireUserPenalty.findByQuestionnaireProblemAndQuestionnaireUser(
                            questionnaireProblem,
                            QuestionnaireUser.findByUserAndQuestionnaire(
                                    user,
                                    questionnaire
                            )
                    )?.penalty
            ]

            total += ((data.score as Double) - ((data.penalty ?: 0) as Double))
            returnData[problem.id] = data
        }

        returnData["score"] = total

        returnData

    }

    def userProblemContext(List<Submission> submissions, Date startDate = null, Date endDate = null) {
        def submissionsData = extractSubmissionsData(submissions, startDate, endDate)

        def status = null

        if(!submissionsData.lastCorrectSubmissions.empty) {
            status = Submission.Evaluation.CORRECT
        } else if (!submissionsData.lastSubmissions.empty) {
            status = (submissionsData.lastSubmissions.first() as Submission).evaluation
        }


        return [
                submissionsCount              : submissionsData.submissionsCount,
                submissionsCountByLanguage    : submissionsData.submissionsCountByLanguage,
                submissionsCountByEvaluation  : submissionsData.submissionsCountByEvaluation,
                submissionsCountHistory       : submissionsData.submissionsCountHistory,
                lastSubmissions               : submissionsData.lastSubmissions,
                lastCorrectSubmissions        : submissionsData.lastCorrectSubmissions,
                fastestSubmissions            : submissionsData.fastestSubmissions,
                status                        : status
        ]
    }

    def questionnaireContext(Questionnaire questionnaire, List<Submission> submissions, Date startDate = null, Date endDate = null) {

        def problems = QuestionnaireProblem.findAllByQuestionnaire(questionnaire).problem
        def submissionsInQuestionnaire = submissions.findAll { problems.contains(it.problem) }
        def usersScores = [:]

        def submissionsData = extractSubmissionsData(
                submissionsInQuestionnaire,
                startDate,
                endDate
        )


        questionnaire.users.each { User user ->
            usersScores.put(user.id, [:])
            def total = 0
            problems.each { Problem problem ->

                def questionnaireProblem = QuestionnaireProblem.findByQuestionnaireAndProblem(questionnaire, problem)

                def evaluation

                def submission = Submission.findByProblemAndUserAndEvaluationAndSubmissionDateLessThan(
                        problem,
                        user,
                        Submission.Evaluation.CORRECT,
                        questionnaire.endDate
                )

                if (submission) {
                    evaluation = submission.evaluation
                } else {
                    evaluation = Submission.findByProblemAndUserAndSubmissionDateLessThan(
                            problem,
                            user,
                            questionnaire.endDate,
                            [sort: "submissionDate", order: "desc"]
                    )?.evaluation
                }

                def data = [
                        status : evaluation,
                        score  : evaluation == Submission.Evaluation.CORRECT ? questionnaireProblem.score : 0,
                        penalty: QuestionnaireUserPenalty.findByQuestionnaireProblemAndQuestionnaireUser(
                                questionnaireProblem,
                                QuestionnaireUser.findByUserAndQuestionnaire(
                                        user,
                                        questionnaire
                                )
                        )?.penalty
                ]

                total += ((data.score as Double) - ((data.penalty ?: 0) as Double))
                usersScores.get(user.id).put(problem.id, data)
            }

            usersScores.get(user.id).put("score", total)
        }

        return [
                submissionsCount              : submissionsData.submissionsCount,
                usersWhoTried                 : submissionsData.usersWhoTried,
                usersWhoTriedCount            : submissionsData.usersWhoTriedCount,
                usersWhoSolved                : submissionsData.usersWhoSolved,
                usersWhoSolvedCount           : submissionsData.usersWhoSolvedCount,
                triedProblems                 : submissionsData.triedProblems,
                triedProblemsCount			  : submissionsData.triedProblemsCount,
                solvedProblems  			  : submissionsData.solvedProblems,
                solvedProblemsCount			  : submissionsData.solvedProblemsCount,
                ndCount						  : submissionsData.ndCount,
                submissionsCountByLanguage    : submissionsData.submissionsCountByLanguage,
                submissionsCountByEvaluation  : submissionsData.submissionsCountByEvaluation,
                solvedProblemsCountByTopic    : submissionsData.solvedProblemsCountByTopic,
                solvedProblemsCountByNd		  : submissionsData.solvedProblemsCountByNd,
                submissionsCountHistory       : submissionsData.submissionsCountHistory,
                submissionsCountByProblem	  : submissionsData.submissionsCountByProblem,
                usersWhoTriedByProblemCount   : submissionsData.usersWhoTriedByProblemCount,
                usersWhoTriedByProblem        : submissionsData.usersWhoTriedByProblem,
                usersWhoSolvedByProblemCount  : submissionsData.usersWhoSolvedByProblemCount,
                usersWhoSolvedByProblem       : submissionsData.usersWhoSolvedByProblem,
                lastSubmissions               : submissionsData.lastSubmissions,
                lastCorrectSubmissions        : submissionsData.lastCorrectSubmissions,
                fastestSubmissions            : submissionsData.fastestSubmissions,
                ndCountHistory				  : submissionsData.ndCountHistory,
                usersScores				      : usersScores
        ]
    }

    def groupContext(Group group, List<Submission> submissions, Date startDate = null, Date endDate = null) {
        def submissionsData = extractSubmissionsData(submissions, startDate, endDate)

        def now = new Date()

        def openQuizzesCount = Questionnaire.countByStartDateGreaterThanAndEndDateLessThanAndGroup(now, now, group)
        def quizzesCount = Questionnaire.countByGroup(group)
        def closedQuizzesCount = quizzesCount - openQuizzesCount
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis()

        if (!startDate || !endDate) {
            endDate = submissions.get(0).submissionDate
            startDate = endDate - DEFAULT_HISTORY_RANGE
        }

        def accessCountHistory =  emptyDateTimeISOMap(startDate, endDate)
        def openQuizzesCountHistory = emptyDateTimeISOMap(startDate, endDate)

        def userGroups = UserGroup.findAllByGroupAndRole(group, UserGroup.Role.STUDENT)

        AuthenticationHistory.createCriteria().list() {
            if (userGroups && !userGroups.empty) {
                inList("user", userGroups)
            }
            between("accessedDate", startDate, endDate)
        }.each {
            def key = new DateTime(it.accessedDate).withTimeAtStartOfDay().toString(formatter)

            if (accessCountHistory.containsKey(key)) {
                accessCountHistory[key]++
            }
        }
        Questionnaire.findAllByGroup(group).each { Questionnaire questionnaire ->
            (0..(questionnaire.endDate - questionnaire.startDate)).each {
                def key = new DateTime(questionnaire.startDate.plus(it as Integer)).withTimeAtStartOfDay().toString(formatter)
                if (openQuizzesCountHistory.containsKey(key)) {
                    openQuizzesCountHistory[key]++
                }
            }
        }

        def studentCount = UserGroup.countByGroupAndRole(group, UserGroup.Role.STUDENT)
        def teacherCount = UserGroup.countByGroupAndRole(group, UserGroup.Role.TEACHER)
        def teacherAssistantCount = UserGroup.countByGroupAndRole(group, UserGroup.Role.TEACHER_ASSISTANT)


        return [
                submissionsCount              : submissionsData.submissionsCount,
                usersWhoTriedCount            : submissionsData.usersWhoTriedCount,
                usersWhoSolvedCount           : submissionsData.usersWhoSolvedCount,
                triedProblemsCount			  : submissionsData.triedProblemsCount,
                solvedProblemsCount			  : submissionsData.solvedProblemsCount,
                ndCount						  : submissionsData.ndCount,
                submissionsCountByLanguage    : submissionsData.submissionsCountByLanguage,
                submissionsCountByEvaluation  : submissionsData.submissionsCountByEvaluation,
                solvedProblemsCountByTopic    : submissionsData.solvedProblemsCountByTopic,
                solvedProblemsCountByNd		  : submissionsData.solvedProblemsCountByNd,
                submissionsCountHistory       : submissionsData.submissionsCountHistory,
                usersWhoTriedCountHistory     : submissionsData.usersWhoTriedCountHistory,
                submissionsCountByProblem	  : submissionsData.submissionsCountByProblem,
                usersWhoTriedByProblemCount   : submissionsData.usersWhoTriedByProblemCount,
                usersWhoSolvedByProblemCount  : submissionsData.usersWhoSolvedByProblemCount,
                lastSubmissions               : submissionsData.lastSubmissions,
                lastCorrectSubmissions        : submissionsData.lastCorrectSubmissions,
                fastestSubmissions            : submissionsData.fastestSubmissions,
                ndCountHistory				  : submissionsData.ndCountHistory,
                openQuizzesCount              : openQuizzesCount,
                closedQuizzesCount            : closedQuizzesCount,
                quizzesCount                  : quizzesCount,
                accessCountHistory            : accessCountHistory,
                studentCount				  : studentCount,
                teacherCount                  : teacherCount,
                teacherAssistantCount         : teacherAssistantCount,
                openQuizzesCountHistory       : openQuizzesCountHistory,
                submissionsCountByUser        : submissionsData.submissionsCountByUser,
                problemsSolvedCountByUser     : submissionsData.problemsSolvedCountByUser,
                problemsTriedCountByUser      : submissionsData.problemsTriedCountByUser,
                topicsTriedCountByUser        : submissionsData.topicsTriedCountByUser,
                topicsSolvedCountByUser       : submissionsData.topicsSolvedCountByUser
        ]
    }

    Map extractSubmissionsData(List<Submission> submissions, Map data Date startDate = null, Date endDate = null) {
        def data = [
                submissionsCount              : 0,
                usersWhoTriedCount            : 0,
                usersWhoTried                 : [],
                usersWhoSolvedCount           : 0,
                usersWhoSolved                 : [],
                triedProblems   			  : [],
                triedProblemsCount			  : 0,
                solvedProblems                : [],
                solvedProblemsCount			  : 0,
                ndCount						  : 0,
                submissionsCountByLanguage    : emptySubmissionsByLanguageMap(),
                submissionsCountByEvaluation  : emptySubmissionsByEvaluationMap(),
                submissionsCountByUser        : [:],
                problemsSolvedCountByUser     : [:],
                problemsTriedCountByUser      : [:],
                topicsTriedCountByUser        : [:],
                topicsSolvedCountByUser       : [:],
                solvedProblemsCountByTopic    : [:],
                solvedProblemsCountByNd		  : [:],
                submissionsCountHistory       : [:],
                submissionsCountByProblem	  : [:],
                usersWhoTriedByProblem        : [:],
                usersWhoTriedByProblemCount   : [:],
                usersWhoSolvedByProblem       : [:],
                usersWhoSolvedByProblemCount  : [:],
                lastSubmissions               : [],
                lastCorrectSubmissions        : [],
                fastestSubmissions            : new ArrayList<Submission>(),
                ndCountHistory				  : [:],
                usersWhoTriedCountHistory     : [:]
        ]

        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis()
        def evaluationMap = emptySubmissionsByEvaluationMap()
        def usersWhoTried = []
        def usersWhoSolved = []
        def correctSubmissions = []
        def triedProblems = []
        def solvedProblems = []
        HashMap<String, List> usersWhoTriedCountHistory = [:]
        HashMap<Problem, List> userWhoTriedProblem = [:]
        HashMap<Problem, List> userWhoSolvedProblem = [:]



        if (!submissions.empty) {
            if (!startDate || !endDate) {
                endDate = submissions.get(0).submissionDate
                startDate = endDate - DEFAULT_HISTORY_RANGE
            }

            data.submissionsCountHistory = emptySubmissionsHistoryMap(startDate, endDate)
            data.usersWhoTriedCountHistory = emptyDateTimeISOMap(startDate, endDate)
            usersWhoTriedCountHistory = emptyDateTimeListISOMap(startDate, endDate)

            def ndCountHistory = emptyDateTimeMap(endDate, submissions.last().submissionDate)


            submissions.each { Submission submission ->

                data.submissionsCount++
                def submissionUserInfo = userInfo(submission.user)
                def submissionProblemInfo = problemInfo(submission.problem)
                if (!data.submissionsCountByUser.containsKey(submissionUserInfo)) {
                    data.submissionsCountByUser[submissionUserInfo] = 0
                }
                data.submissionsCountByUser[submissionUserInfo] = data.submissionsCountByUser[submissionUserInfo] + 1;
                if (submission.evaluation.equals(Submission.Evaluation.CORRECT)) {
                    if (!data.problemsSolvedCountByUser.containsKey(submissionUserInfo)) {
                        data.problemsSolvedCountByUser[submissionUserInfo] = []
                    }
                    if (!data.problemsSolvedCountByUser[submissionUserInfo].contains(submissionProblemInfo)) {
                        data.problemsSolvedCountByUser[submissionUserInfo].add(submissionProblemInfo)
                        submission.problem.topics.each {
                            def topic = topicInfo(it)
                            if (!data.topicsSolvedCountByUser.containsKey(topic)) {
                                data.topicsSolvedCountByUser[topic] = []
                            }
                            if (!data.topicsSolvedCountByUser[topic].contains(submissionUserInfo)) {
                                data.topicsSolvedCountByUser[topic].add(submissionUserInfo)
                            }
                        }
                    }
                }
                if (!data.problemsTriedCountByUser.containsKey(submissionUserInfo)) {
                    data.problemsTriedCountByUser[submissionUserInfo] = []
                }
                if (!data.problemsTriedCountByUser[submissionUserInfo].contains(submissionProblemInfo)) {
                    data.problemsTriedCountByUser[submissionUserInfo].add(submissionProblemInfo)
                    submission.problem.topics.each {
                        def topic = topicInfo(it)
                        if (!data.topicsTriedCountByUser.containsKey(topic)) {
                            data.topicsTriedCountByUser[topic] = []
                        }
                        if (!data.topicsTriedCountByUser[topic].contains(submissionUserInfo)) {
                            data.topicsTriedCountByUser[topic].add(submissionUserInfo)
                        }
                    }
                }
                if (!userWhoTriedProblem[problemInfo(submission.problem)]) {
                    userWhoTriedProblem.put(problemInfo(submission.problem), [])
                }

                if (!userWhoTriedProblem.get(problemInfo(submission.problem)).contains(userInfo(submission.user))) {
                    userWhoTriedProblem.get(problemInfo(submission.problem)).add(userInfo(submission.user))

                    if (!data.usersWhoTriedByProblemCount[submission.problem.id]) {
                        data.usersWhoTriedByProblemCount[submission.problem.id] = 0
                    }

                    data.usersWhoTriedByProblemCount[submission.problem.id]++
                }



                if (submission.evaluation == Submission.Evaluation.CORRECT) {
                    if (!userWhoSolvedProblem[problemInfo(submission.problem)]) {
                        userWhoSolvedProblem.put(problemInfo(submission.problem), [])
                    }

                    if (!userWhoSolvedProblem.get(problemInfo(submission.problem)).contains(userInfo(submission.user))) {
                        userWhoSolvedProblem.get(problemInfo(submission.problem)).add(userInfo(submission.user))

                        if (!data.usersWhoSolvedByProblemCount[submission.problem.id]) {
                            data.usersWhoSolvedByProblemCount[submission.problem.id] = 0
                        }

                        data.usersWhoSolvedByProblemCount[submission.problem.id]++
                    }
                }

                if (!usersWhoTried.contains(userInfo(submission.user))) {
                    usersWhoTried.add(userInfo(submission.user))
                    data.usersWhoTriedCount++
                }

                def usersWhoTriedCountHistoryKey = new DateTime(submission.submissionDate).withTimeAtStartOfDay()
                        .toString(formatter)

                if(!usersWhoTriedCountHistory[usersWhoTriedCountHistoryKey]?.contains(userInfo(submission.user))) {
                    usersWhoTriedCountHistory[usersWhoTriedCountHistoryKey]?.add(userInfo(submission.user))
                    if (data.usersWhoTriedCountHistory.containsKey(usersWhoTriedCountHistoryKey)) {
                        data.usersWhoTriedCountHistory[usersWhoTriedCountHistoryKey]++
                    }
                }

                if (!usersWhoSolved.contains(userInfo(submission.user)) &&
                        (submission.evaluation == Submission.Evaluation.CORRECT)) {
                    usersWhoSolved.add(userInfo(submission.user))
                    data.usersWhoSolvedCount++
                }

                if (!triedProblems.contains(problemInfo(submission.problem))) {
                    triedProblems.add(problemInfo(submission.problem))
                    data.triedProblemsCount++
                }

                if (!solvedProblems.contains(problemInfo(submission.problem)) &&
                        (submission.evaluation == Submission.Evaluation.CORRECT)) {
                    solvedProblems.add(problemInfo(submission.problem))
                    data.solvedProblemsCount++
                    data.ndCount += submission.problem.nd

                    for (Topic topic : submission.problem.topics) {
                        if (!data.solvedProblemsCountByTopic[topic.name]) {
                            data.solvedProblemsCountByTopic.put(topic.name, 0)
                        }

                        data.solvedProblemsCountByTopic[topic.name]++
                    }

                    if (!data.solvedProblemsCountByNd[submission.problem.nd]) {
                        data.solvedProblemsCountByNd.put(submission.problem.nd, 0)
                    }
                    data.solvedProblemsCountByNd[submission.problem.nd]++

                    def key = new DateTime(submission.submissionDate).withTimeAtStartOfDay()

                    if (ndCountHistory.containsKey(key)) {
                        ndCountHistory[key] += submission.problem.nd
                    }
                }

                data.submissionsCountByLanguage[submission.language.label][submission.evaluation as String]++
                data.submissionsCountByLanguage[submission.language.label]["TOTAL"]++

                data.submissionsCountByEvaluation[submission.evaluation as String]++
                data.submissionsCountByEvaluation["TOTAL"]++

                def key = new DateTime(submission.submissionDate).withTimeAtStartOfDay().toString(formatter)
                if (data.submissionsCountHistory.containsKey(key)) {
                    data.submissionsCountHistory[key][submission.evaluation as String]++
                    data.submissionsCountHistory[key]["TOTAL"]++
                }

                if (submission.problem) {
                    if (!data.submissionsCountByProblem[submission.problem.name]) {
                        data.submissionsCountByProblem.put(submission.problem.name, evaluationMap.clone())
                    }

                    data.submissionsCountByProblem[submission.problem.name][submission.evaluation as String]++
                    data.submissionsCountByProblem[submission.problem.name]["TOTAL"]++
                }

                if ((submission.evaluation == Submission.Evaluation.CORRECT) && (submission.time > 0)) {
                    correctSubmissions.add(submissionInfo(submission))

                    if (correctSubmissions.size() - 1 <= MAX_LAST_CORRECT_SUBMISSIONS) {
                        data.lastCorrectSubmissions.add(submissionInfo(submission))
                    }
                }

                if (data.submissionsCount - 1 <= MAX_LAST_SUBMISSIONS) {
                    data.lastSubmissions.add(submissionInfo(submission))
                }
            }


//			correctSubmissions.sort { a, b -> a.time == b.time ? 0 : a.time < b.time ? -1 : 1 }
//
//
//			Iterator<Submission> correctSubmissionsIterator = correctSubmissions.iterator()
//			while (data.fastestSubmissions.size() <= MAX_FASTEST_SUBMISSIONS && correctSubmissionsIterator.hasNext()) {
//				Submission submission = correctSubmissionsIterator.next()
//				if (!data.fastestSubmissions.user.id.contains(submission.user.id)
//						&& !submission.user.authorities.authority.contains("ROLE_ADMIN")) {
//					data.fastestSubmissions.add(submissionInfo(submission))
//				}
//			}

            def keys = ndCountHistory.keySet().sort()
            def acc = 0
            def size = keys.size()
            keys.eachWithIndex { key, i ->
                acc += ndCountHistory.get(key)
                if ((size - i) <= Math.min(endDate - startDate, MAX_HISTORY_RANGE)) {
                    data.ndCountHistory.put(((key as DateTime).toString(formatter)), acc)
                }
            }
        }

        return data
    }

    Map emptyDateTimeMap(Date startDate, Date endDate) {
        def map = [:]

        (0..(endDate - startDate)).each {
            map.put(new DateTime(endDate - (it as Integer)).withTimeAtStartOfDay(), 0)
        }

        return map
    }

    Map emptyDateTimeISOMap(Date startDate, Date endDate) {
        def map = [:]
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis()

        (0..(endDate - startDate)).each {
            map.put(new DateTime(endDate - (it as Integer)).withTimeAtStartOfDay().toString(formatter), 0)
        }

        return map
    }

    Map emptyDateTimeListISOMap(Date startDate, Date endDate) {
        def map = [:]
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis()

        (0..(endDate - startDate)).each {
            map.put(new DateTime(endDate - (it as Integer)).withTimeAtStartOfDay().toString(formatter), [])
        }

        return map
    }


    Map emptySubmissionsByLanguageMap() {
        def map = [:]
        def evaluationMap = emptySubmissionsByEvaluationMap()

        Language.list().each {
            map.put(it.label, evaluationMap.clone())
        }

        return map
    }

    Map emptySubmissionsByEvaluationMap() {
        def map = [:]

        map.put('TOTAL', 0)

        Submission.Evaluation.values().each {
            map.put(it as String, 0)
        }

        return map
    }

    Map emptySubmissionsHistoryMap(Date startDate, Date endDate) {

        def map = [:]
        def HISTORY_RANGE = Math.min(endDate - startDate, MAX_HISTORY_RANGE)
        def evaluationMap = emptySubmissionsByEvaluationMap()
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis()

        (0..HISTORY_RANGE).each {
            def key = new DateTime(endDate - it).withTimeAtStartOfDay().toString(formatter)
            map.put(key, evaluationMap.clone())
        }

        return map
    }

    Closure getSubmissionCriteria(Map params) {
        return {
            and {
                if (params.problem) {
                    eq("problem", params.problem)
                }

                if (params.user) {
                    eq("user", params.user)
                } else if (params.users && !params.users.empty) {
                    inList("user", params.users)
                }


                if (params.startDate && params.endDate) {
                    between("submissionDate", params.startDate, params.endDate)
                }
            }
        }
    }

    def userInfo(User user) {
        return [
                id: user.id,
                name: user.name,
                avatar: "https://www.thehuxley.com/api/v1/users/avatar/$user.avatar"
        ]
    }

    def problemInfo(Problem problem) {
        return [
                id: problem.id,
                name: problem.name
        ]
    }

    def topicInfo(Topic topic) {
        return [
                id: topic.id,
                name: topic.name
        ]
    }

    def submissionInfo(Submission submission) {
        return [
                id: submission.id,
                problem: problemInfo(submission.problem),
                user: userInfo(submission.user),
                time: submission.time,
                language: [id: submission.language.id, label: submission.language.label],
                evaluation: submission.evaluation.toString()
        ]
    }
*/
    @Autowired SubmissionRepository submissionRepository
    @Autowired ConfigRepository configRepository
    @Autowired ProblemRepository problemRepository
    @Autowired TopicRepository topicRepository
    @Autowired UserRepository userRepository
    @Autowired LanguageRepository languageRepository
    @Autowired StatisticRepository statisticRepository
    def Map mapCountByLanguageAndEvaluation() {
        Map map = [:]
        List<Language> languageList = languageRepository.findAll()
        languageList.each {
            map[it.id] = mapCountByEvaluation()
        }
        return map
    }

    def Map mapCountByEvaluation() {
        Map map = [:]
        Submission.Evaluation.values().each {
            map[it as String] = 0
        }
        map["TOTAL"] = 0
        return map
    }

    def Map mapCountByNd() {
        Map map = [:]
        (1..10).each() {
            map[it] = 0
        }
        return map
    }
    def Map extractSubmissionsData(List<Submission> submissionList) {
        def response = [
                submissionsCount              : 0, //Ok
                usersWhoTried                 : [],//Ok
//            usersWhoTriedCount            : 0, Deprecated
                usersWhoSolved                 : [], //Ok
//            usersWhoSolvedCount           : 0, Deprecated
                triedProblems   			  : [], //Ok
//            triedProblemsCount			  : 0, Deprecated
                solvedProblems                : [], //Ok
//            solvedProblemsCount			  : 0, Deprecated
//            ndCount						  : 0, //Ok
                submissionsCountByLanguageAndEvaluation    : mapCountByLanguageAndEvaluation(), //Ok
                submissionsCountByEvaluation  : mapCountByEvaluation(), //Ok
                submissionsCountByUser        : [:],//Ok
//            problemsSolvedCountByUser     : [:], Duplicado
//            problemsTriedCountByUser      : [:], Duplicado
//                userTriedTopicsCount        : [:], //Não aparece os tópicos
//                userSolvedTopicsCount       : [:], //Não aparece os tópicos
//                solvedProblemsCountByTopic    : [:], //Ok
//                solvedProblemsCountByNd		  : [:], //Ok
//                triedProblemsCountByNd		  : [:], //Ok
                submissionsCountHistoryByEvaluation : [:], //Ok
                submissionsCountByProblemAndEvaluation : [:], //Ok
//                usersWhoTriedByProblem        : [:], //Ok
                problemHistory           : [:], //Consertar topic info
//            usersWhoTriedByProblemCount   : [:], Deprecated
                problemsSolvedByUser       : [:], //Ok
//                usersWhoSolvedByProblemCount  : [:], //Deprecated
                lastSubmissions               : [], //Consertar infos
                lastCorrectSubmissions        : [], //Consertar infos
                fastestSubmissions            : [], //Consertar infos
                ndCountHistory				  : [:], //Ok
                usersWhoTriedCountHistory     : [:] //Ok
        ]
        extractSubmissionsData(submissionList, response)
    }
    def Map extractSubmissionsData(List<Submission> submissionList, Map response) {
        submissionList.each() { submission ->
            updateBySubmission(submission, response)
        }

        return response
    }
    def update(String json, boolean isReavaluation = false) {
        update(JSON.parse(json), isReavaluation = false)
    }
    def update(Map json, boolean isReavaluation = false) {
        Submission submissionInstance = submissionRepository.findById(json.id)
        if (!submissionInstance) {
            submissionInstance = new Submission()
        }
        submissionInstance.id = json.id as Long
        submissionInstance.evaluation = Submission.Evaluation.valueOf(json.evaluation as String)
        submissionInstance.problemId = json.problem.id
        submissionInstance.submissionDate = formatter.parseDateTime(json.submissionDate as String).toDate()
        submissionInstance.time = json.time as Double
        submissionInstance.tries = json.tries as Integer
        submissionInstance.userId = json.user.id
        submissionInstance.languageId = json.language.id

        submissionRepository.save(submissionInstance)

        Problem problem
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


        if (!userRepository.findById(json.user.id as Long)) {
            userRepository.save(
                    new User(
                            id: json.user.id,
                            name: json.user.name,
                            avatar: json.user.avatar
                    )
            )
        }

        if (!languageRepository.findById(json.language.id as Long)) {
            languageRepository.save(
                    new Language(
                            id: json.language.id,
                            name: json.language.name,
                    )
            )
        }

        update(submissionInstance, isReavaluation)
    }

    def update(Submission submission, boolean isReavaluation = false) {
        def userId = submission.userId
        def problemId = submission.problemId
        def submissionDate = submission.submissionDate

        def userStatistic = statisticRepository.findByIdAndEntity(userId, Statistic.Entity.USER)
        if (userStatistic != null) {
            if (isReavaluation) {
                statisticRepository.delete(userStatistic)
            } else {
                if (submissionDate > userStatistic.lasUpdated) {
                    userStatistic.lasUpdated = submissionDate
                    userStatistic.statistics = updateBySubmission(submission, userStatistic.statistics)
                    statisticRepository.save(userStatistic)
                }
            }
        }

        def problemStatistic = statisticRepository.findByIdAndEntity(problemId, Statistic.Entity.PROBLEM)
        if (problemStatistic != null) {
            if (isReavaluation) {
                statisticRepository.delete(problemStatistic)
            } else {
                if (submissionDate > problemStatistic.lasUpdated) {
                    problemStatistic.lasUpdated = submissionDate
                    problemStatistic.statistics = updateBySubmission(submission, problemStatistic.statistics)
                    statisticRepository.save(problemStatistic)
                }
            }
        }

        List<Statistic> groupStatistics = statisticRepository.findAllByUsersAndStartDateGreaterThanEqualAndEndDateLessThanEqualAndEntity(userId, submissionDate, submissionDate, Statistic.Entity.GROUP)
        groupStatistics.each { groupStatistic ->
            if (groupStatistic != null) {
                if (isReavaluation) {
                    statisticRepository.delete(groupStatistic)
                } else {
                    if (submissionDate > groupStatistic.lasUpdated) {
                        groupStatistic.lasUpdated = submissionDate
                        groupStatistic.statistics = updateBySubmission(submission, groupStatistic.statistics)
                        statisticRepository.save(groupStatistic)
                    }
                }
            }
        }

        List<Statistic> quizStatistics = statisticRepository.findAllByUsersAndProblemsAndStartDateGreaterThanEqualAndEndDateLessThanEqualAndEntity(userId, problemId, submissionDate, submissionDate, Statistic.Entity.GROUP)
        quizStatistics.each { quizStatistic ->
            if (quizStatistic != null) {
                if (isReavaluation) {
                    statisticRepository.delete(quizStatistic)
                } else {
                    if (submissionDate > quizStatistic.lasUpdated) {
                        quizStatistic.lasUpdated = submissionDate
                        quizStatistic.statistics = updateBySubmission(submission, quizStatistic.statistics)
                        statisticRepository.save(quizStatistic)
                    }
                }
            }
        }
    }

    def Map updateBySubmission(Submission submission, Map response) {
        def user = submission.userId
        def problem = submission.problemId
        def language = submission.languageId
        def evaluation = submission.evaluation as String
        def now = new DateTime(submission.submissionDate).withTimeAtStartOfDay().toString(formatter)

        //Atualizando contagem de submissões
        if (response.submissionsCount != null) {
            response.submissionsCount ++
        }


        //Atualizando a lista de usuarios que tentaram
        if (response.usersWhoTried != null && !response.usersWhoTried.contains(user)) {
            response.usersWhoTried.add(user)
        }

        //Atualizando a lista de problemas tentados
        if (response.triedProblems != null && !response.triedProblems.contains(problem)) {
            response.triedProblems.add(problem)
        }

        //Atualizando a contagem por linguagem
        if (response.submissionsCountByLanguageAndEvaluation != null) {
            if (!response.submissionsCountByLanguageAndEvaluation.containsKey(language)) {
                response.submissionsCountByLanguageAndEvaluation[language] = mapCountByEvaluation()
            }
            if (!response.submissionsCountByLanguageAndEvaluation[language].containsKey(evaluation)) {
                response.submissionsCountByLanguageAndEvaluation[language][evaluation] = 0
            }
            response.submissionsCountByLanguageAndEvaluation[language][evaluation]++
            response.submissionsCountByLanguageAndEvaluation[language]["TOTAL"]++
        }

        //Atualizando a contagem por avaliaçao
        if (response.submissionsCountByEvaluation != null) {
            if (!response.submissionsCountByEvaluation.containsKey(evaluation)) {
                response.submissionsCountByEvaluation[evaluation] = 0
            }
            response.submissionsCountByEvaluation[evaluation]++
            response.submissionsCountByEvaluation["TOTAL"]++
        }

        //Atualizando contagem de submissões por usuário
        if (response.submissionsCountByUser != null) {
            if (!response.submissionsCountByUser.containsKey(user)) {
                response.submissionsCountByUser[user] = 0
            }
            response.submissionsCountByUser[user]++
        }

        //Atualizando histórico de contagem de submission por avaliação
        if (response.submissionsCountHistoryByEvaluation != null) {
            if (!response.submissionsCountHistoryByEvaluation.containsKey(now)) {
                response.submissionsCountHistoryByEvaluation[now] = mapCountByEvaluation()
            }
            if (!response.submissionsCountHistoryByEvaluation[now].containsKey(evaluation)) {
                response.submissionsCountHistoryByEvaluation[now][evaluation] = 0
            }
            response.submissionsCountHistoryByEvaluation[now][evaluation]++
            response.submissionsCountHistoryByEvaluation[now]["TOTAL"]++
        }

        //Atualizando contagem de submissões por problema e por avaliação
        if (response.submissionsCountByProblemAndEvaluation != null) {
            if (!response.submissionsCountByProblemAndEvaluation.containsKey(problem)) {
                response.submissionsCountByProblemAndEvaluation[problem] = mapCountByEvaluation()
            }
            if (!response.submissionsCountByProblemAndEvaluation[problem].containsKey(evaluation)) {
                response.submissionsCountByProblemAndEvaluation[problem][evaluation] = 0
            }
            response.submissionsCountByProblemAndEvaluation[problem][evaluation]++
            response.submissionsCountByProblemAndEvaluation[problem]["TOTAL"]++
        }

        //Atualizando lista de usuários que tentaram por problema
        if (response.problemsTriedByUser != null) {
            if (!response.problemsTriedByUser.containsKey(user)) {
                response.problemsTriedByUser[user] = []
            }
            if (!response.problemsTriedByUser[user].contains(problem)) {
                response.problemsTriedByUser[user].add(problem)
            }
        }

        //Atualizando histórico dos usuários que tentaram
        if (response.usersWhoTriedCountHistory != null) {
            if (!response.usersWhoTriedCountHistory.containsKey(now)) {
                response.usersWhoTriedCountHistory[now] = []
            }
            if (!response.usersWhoTriedCountHistory[now].contains(user)) {
                response.usersWhoTriedCountHistory[now].add(user)
            }
        }

        //Atualizando as informaçoes relativas a resolver o problema
        if (evaluation.equals(Submission.Evaluation.CORRECT as String)) {

            //Atualizando a lista de usuarios que resolveram
            if (response.usersWhoSolved != null && !response.usersWhoSolved.contains(user)) {
                response.usersWhoSolved.add(user)
            }

            //Atualizando a lista de problemas resolvidos
            if (response.solvedProblems != null && !response.solvedProblems.contains(problem)) {
                response.solvedProblems.add(problem)
            }

            //Atualizando a lista de usuários que resolveram por problema
            if (response.problemsSolvedByUser != null) {
                if (!response.problemsSolvedByUser.containsKey(user)) {
                    response.problemsSolvedByUser[user] = []
                }
                if (!response.problemsSolvedByUser[user].contains(problem)) {
                    response.problemsSolvedByUser[user].add(problem)
                }
            }

            if (response.problemHistory != null) {
                if (!response.problemHistory.containsKey(now)) {
                    response.problemHistory[now] = []
                }

                if (!response.problemHistory[now].contains(problem)) {
                    response.problemHistory[now].add(problem)
                }
            }

            //Atualizando a lista de últimas submissões corretas
            if (response.lastCorrectSubmissions != null) {
                response.lastCorrectSubmissions.add(submission)
                if(response.lastCorrectSubmissions.size() > 10) {
                    response.lastCorrectSubmissions.remove(0)
                }
            }

            //Atualizando a lista de submissões mais rápidas
            if (response.fastestSubmissions != null) {
                response.fastestSubmissions.add(submission)
                response.fastestSubmissions = response.fastestSubmissions.sort { a, b -> a.time == b.time ? 0 : a.time < b.time ? -1 : 1 }
                if(response.fastestSubmissions.size() > 10) {
                    response.fastestSubmissions.remove(0)
                }
            }
        } //Evaluation is correct

        if (response.lastSubmissions != null) {
            response.lastSubmissions.add(submission)
            if(response.lastSubmissions.size() > 10) {
                response.lastSubmissions.remove(0)
            }
        }
        return response
    }

    def Map getResponse(response) {
        def tempResponse = [:]
        if (response) {
            if (response.submissionsCount != null) {
                tempResponse.submissionsCount = response.submissionsCount
            }

            //Atualizando a lista de usuarios que tentaram
            if (response.usersWhoTried != null) {
                tempResponse.usersWhoTried = []
                response.usersWhoTried.each { user ->
                    tempResponse.usersWhoTried.add(userInfo(user))
                }
            }

            //Atualizando a lista de problemas tentados
            if (response.triedProblems != null) {
                tempResponse.triedProblems = []
                response.triedProblems.each() { problem ->
                    tempResponse.triedProblems.add(problemRepository.findById(problem))
                }
            }

            if (response.triedProblemsCountByNd != null) {
                tempResponse.triedProblemsCountByNd = mapCountByNd()
                tempResponse.triedProblems.each { problem ->
                    tempResponse.triedProblemsCountByNd[problem.nd]++
                }
            }

            if (response.triedProblemsCountByTopic != null) {
                tempResponse.triedProblemsCountByTopic = [:]
                tempResponse.triedProblems.each { problem ->
                    problem.topics.each() { topic ->
                        if (!tempResponse.triedProblemsCountByTopic.containsKey(topicInfo(topic.id))) {
                            tempResponse.triedProblemsCountByTopic[topicInfo(topic.id)] = 0
                        }
                        tempResponse.triedProblemsCountByTopic[topicInfo(topic.id)]++
                    }
                }
            }

            //Atualizando a contagem por linguagem
            if (response.submissionsCountByLanguageAndEvaluation != null) {
                tempResponse.submissionsCountByLanguageAndEvaluation = [:]
                response.submissionsCountByLanguageAndEvaluation.keySet().each() { language ->
                    tempResponse.submissionsCountByLanguageAndEvaluation[languageInfo(language)] = response.submissionsCountByLanguageAndEvaluation[language]
                }
            }

            //Atualizando a contagem por avaliaçao
            if (response.submissionsCountByEvaluation != null) {
                tempResponse.submissionsCountByEvaluation = response.submissionsCountByEvaluation
            }

            //Atualizando contagem de submissões por usuário
            if (response.submissionsCountByUser != null) {
                tempResponse.submissionsCountByUser = [:]
                response.submissionsCountByUser.keySet().each() { user ->
                    tempResponse.submissionsCountByUser[userInfo(user)] = response.submissionsCountByUser[user]
                }
            }

            //Atualizando histórico de contagem de submission por avaliação
            if (response.submissionsCountHistoryByEvaluation != null) {
                tempResponse.submissionsCountHistoryByEvaluation = response.submissionsCountHistoryByEvaluation
            }

            //Atualizando contagem de submissões por problema e por avaliação
            if (response.submissionsCountByProblemAndEvaluation != null) {
                tempResponse.submissionsCountByProblemAndEvaluation = [:]
                response.submissionsCountByProblemAndEvaluation.keySet().each() { problem ->
                    tempResponse.submissionsCountByProblemAndEvaluation[problemInfo(problem)] = response.submissionsCountByProblemAndEvaluation[problem]
                }
            }

            //Atualizando lista de usuários que tentaram por problema
            if (response.problemsTriedByUser != null) {
                tempResponse.problemsTriedByUser = [:]
                response.problemsTriedByUser.keySet().each() { user ->
                    def tempUser = userInfo(user)
                    if (!tempResponse.problemsTriedByUser.containsKey(tempUser)) {
                        tempResponse.problemsTriedByUser[tempUser] = []
                    }
                    response.problemsTriedByUser[user].each() { problem ->
                        tempResponse.problemsTriedByUser[tempUser].add(problemInfo(problem))
                    }
                }
            }

            //Atualizando histórico dos usuários que tentaram
            if (response.usersWhoTriedCountHistory != null) {
                tempResponse.usersWhoTriedCountHistory = [:]
                response.usersWhoTriedCountHistory.keySet().each() { date ->
                    if (!tempResponse.usersWhoTriedCountHistory.containsKey(date)) {
                        tempResponse.usersWhoTriedCountHistory[date] = []
                    }
                    response.usersWhoTriedCountHistory[date].each() { user ->
                        tempResponse.usersWhoTriedCountHistory[date].add(userInfo(user))
                    }
                }

            }

            //Atualizando a lista de usuarios que resolveram
            if (response.usersWhoSolved != null) {
                tempResponse.usersWhoSolved = []
                response.usersWhoSolved.each() { user ->
                    tempResponse.usersWhoSolved.add(userInfo(user))
                }
            }

            //Atualizando a lista de problemas resolvidos
            if (response.solvedProblems) {
                tempResponse.solvedProblems = []
                response.solvedProblems.each() { problem ->
                    tempResponse.solvedProblems.add(problemRepository.findById(problem))
                }
            }

            //Atualizando a lista de usuários que resolveram por problema
            if (response.problemsSolvedByUser != null) {
                tempResponse.problemsSolvedByUser = [:]
                response.problemsSolvedByUser.keySet().each() { user ->
                    def tempUser = userInfo(user)
                    if (!tempResponse.problemsSolvedByUser.containsKey(user)) {
                        tempResponse.problemsSolvedByUser[tempUser] = []
                    }
                    response.problemsSolvedByUser[user].each() { problem ->
                        tempResponse.problemsSolvedByUser[tempUser].add(problemInfo(problem))
                    }
                }
            }

            if (response.problemHistory != null) {
                tempResponse.problemHistory = [:]
                response.problemHistory.keySet().each() { date ->
                    if (!tempResponse.problemHistory.containsKey(date)) {
                        tempResponse.problemHistory[date] = []
                    }
                    response.problemHistory[date].each() { problem ->
                        def tempProblem = problemRepository.findById(problem)
                        if (!tempResponse.problemHistory[date].contains(tempProblem)) {
                            tempResponse.problemHistory[date].add(tempProblem)
                        }
                    }

                }
            }

            //Atualizando lista de últimas submissões corretas
            if (response.lastCorrectSubmissions != null) {
                tempResponse.lastCorrectSubmissions = []
                response.lastCorrectSubmissions.each() { submission ->
                    def tempSubmission = [:]
                    tempSubmission.sid = submission.sid
                    tempSubmission.id = submission.id
                    tempSubmission.submissionDate = submission.submissionDate
                    tempSubmission.time = submission.time
                    tempSubmission.evaluation = submission.evaluation
                    tempSubmission.user = userInfo(submission.userId)
                    tempSubmission.problem = problemInfo(submission.problemId)
                    tempSubmission.language = languageInfo(submission.languageId)
                    tempResponse.lastCorrectSubmissions.add(tempSubmission)
                }
            }

            //Atualizando lista de submissões mais rápidas
            if (response.fastestSubmissions != null) {
                tempResponse.fastestSubmissions = []
                response.fastestSubmissions.each() { submission ->
                    def tempSubmission = [:]
                    tempSubmission.sid = submission.sid
                    tempSubmission.id = submission.id
                    tempSubmission.submissionDate = submission.submissionDate
                    tempSubmission.time = submission.time
                    tempSubmission.evaluation = submission.evaluation
                    tempSubmission.user = userInfo(submission.userId)
                    tempSubmission.problem = problemInfo(submission.problemId)
                    tempSubmission.language = languageInfo(submission.languageId)
                    tempResponse.fastestSubmissions.add(tempSubmission)
                }
            }

            //Atualizando lista de últimas submissões
            if (response.lastSubmissions != null) {
                tempResponse.lastSubmissions = []
                response.lastSubmissions.each() { submission ->
                    def tempSubmission = [:]
                    tempSubmission.sid = submission.sid
                    tempSubmission.id = submission.id
                    tempSubmission.submissionDate = submission.submissionDate
                    tempSubmission.time = submission.tries
                    tempSubmission.evaluation = submission.evaluation
                    tempSubmission.user = userInfo(submission.userId)
                    tempSubmission.problem = problemInfo(submission.problemId)
                    tempSubmission.language = languageInfo(submission.languageId)
                    tempResponse.lastSubmissions.add(tempSubmission)
                }
            }

            if (tempResponse.solvedProblems != null) {
                if (response.ndCount != null) {
                    tempResponse.ndCount = 0
                    tempResponse.solvedProblems.each { problem ->
                        tempResponse.ndCount += problem.nd
                    }
                }
                if (response.solvedProblemsCountByNd != null) {
                    tempResponse.solvedProblemsCountByNd = mapCountByNd()
                    tempResponse.solvedProblems.each { problem ->
                        tempResponse.solvedProblemsCountByNd[problem.nd]++
                    }
                }

                if (response.solvedProblemsCountByTopic != null) {
                    tempResponse.solvedProblemsCountByTopic = [:]
                    tempResponse.solvedProblems.each { problem ->
                        problem.topics.each() { topic ->
                            if (!tempResponse.solvedProblemsCountByTopic.containsKey(topicInfo(topic.id))) {
                                tempResponse.solvedProblemsCountByTopic[topicInfo(topic.id)] = 0
                            }
                            tempResponse.solvedProblemsCountByTopic[topicInfo(topic.id)]++
                        }
                    }
                }
            }
            if (response.userTriedTopicsCount != null) {
                tempResponse.userTriedTopicsCount = [:]
                tempResponse.problemsTriedByUser.keySet().each() { user ->
                    tempResponse.problemsTriedByUser[user].each() { problem ->
                        if (!tempResponse.userTriedTopicsCount.containsKey(user)) {
                            tempResponse.userTriedTopicsCount[user] = [:]
                        }
                        def tempProblem = problemRepository.findById(problem.id)
                        tempProblem.topics.each() { topic ->
                            if (!tempResponse.userTriedTopicsCount[user].containsKey(topicInfo(topic.id))) {
                                tempResponse.userTriedTopicsCount[user][topicInfo(topic.id)] = 0
                            }
                            tempResponse.userTriedTopicsCount[user][topicInfo(topic.id)]++
                        }

                    }
                }
            }
            if (response.userSolvedTopicsCount != null) {
                tempResponse.userSolvedTopicsCount = [:]
                tempResponse.problemsSolvedByUser.keySet().each() { user ->
                    tempResponse.problemsSolvedByUser[user].each() { problem ->
                        if (!tempResponse.userSolvedTopicsCount.containsKey(user)) {
                            tempResponse.userSolvedTopicsCount[user] = [:]
                        }
                        def tempProblem = problemRepository.findById(problem.id)
                        tempProblem.topics
                        tempProblem.topics.each() { topic ->
                            if (!tempResponse.userSolvedTopicsCount[user].containsKey(topicInfo(topic.id))) {
                                tempResponse.userSolvedTopicsCount[user][topicInfo(topic.id)] = 0
                            }
                            tempResponse.userSolvedTopicsCount[user][topicInfo(topic.id)]++
                        }

                    }
                }
            }
            if (response.ndCountHistory != null && tempResponse.problemHistory != null) {
                tempResponse.ndCountHistory = [:]
                tempResponse.problemHistory.keySet().each() { date ->
                    if (!tempResponse.ndCountHistory.containsKey(date)) {
                        tempResponse.ndCountHistory[date] = 0
                    }
                    tempResponse.problemHistory[date].each() { problem ->
                        tempResponse.ndCountHistory[date] += problem.nd
                    }
                }
            }

            if (tempResponse.solvedProblems) {
                def solvedProblems = []
                tempResponse.solvedProblems.each() { problem ->
                    solvedProblems.add(problemInfo(problem))
                }
                tempResponse.solvedProblems = solvedProblems
            }

            if (tempResponse.triedProblems) {
                def triedProblems = []
                tempResponse.triedProblems.each() { problem ->
                    triedProblems.add(problemInfo(problem))
                }
                tempResponse.triedProblems = triedProblems
            }

            if (tempResponse.problemHistory != null) {
                def problemHistory = [:]
                response.problemHistory.keySet().each() { date ->
                    if (!problemHistory.containsKey(date)) {
                        problemHistory[date] = []
                    }
                    response.problemHistory[date].each() { problem ->
                        def tempProblem = problemInfo(problem)
                        if (!problemHistory[date].contains(tempProblem)) {
                            problemHistory[date].add(tempProblem)
                        }
                    }

                }
            }
            //Exclusivo de grupos
            if (response.closedquizzes != null) {
                tempResponse.closedquizzes = response.closedquizzes
            }

            if (response.openquizzes != null) {
                tempResponse.openquizzes = response.openquizzes
            }

            if (response.openQuizzesHistory != null) {
                tempResponse.openQuizzesHistory = response.openQuizzesHistory
            }

            if (response.quizzes != null) {
                tempResponse.quizzes = response.quizzes
            }

            if (response.student != null) {
                tempResponse.student = response.student
            }

            if (response.teacher != null) {
                tempResponse.teacher = response.teacher
            }

            if (response.teacherAssistant != null) {
                tempResponse.teacherAssistant = response.teacherAssistant
            }

            //Exclusivo de questionário
            if (response.usersScores != null) {
                tempResponse.usersScores = response.usersScores
            }

            if (response.problemList != null) {
                tempResponse.problemList = response.problemList
            }
        }
        return tempResponse
    }

    def userInfo(userId) {
        User user = userRepository.findById(userId as long)
        if (!user) {
            return userId
        }
        return [
                id: user.id,
                name: user.name,
                avatar: user.avatar
        ]

    }

    def problemInfo(problemId) {
        Problem problem = problemRepository.findById(problemId as long)
        if (!problem) {
            return problemId
        }
        return problemInfo(problem)
    }

    def problemInfo(Problem problem) {
        return [
                id: problem.id,
                name: problem.name
        ]
    }

    def topicInfo(topicId) {
        Topic topic = topicRepository.findById(topicId as long)
        if (!topic) {
            return topicId
        }
        return topic.name
    }

    def languageInfo(languageId) {
        Language language = languageRepository.findById(languageId as long)
        if (!language) {
            return languageId
        }
        return [
                id: language.id,
                name: language.name,
                label: language.name
        ]
    }

    def userContext(Submission submission) {
        return userContext([submission])
    }
    def userContext(List<Submission> submissions) {
        def submissionsData = [
                submissionsCount                          : 0, //ok
                solvedProblems                            : [], //ok
                triedProblems			                  : [], //ok
                submissionsCountByLanguageAndEvaluation   : [:], //ok
                submissionsCountByEvaluation              : mapCountByEvaluation(), //ok
                solvedProblemsCountByTopic                : [:], //ok
                submissionsCountHistoryByEvaluation       : [:], //ok
                submissionsCountByProblemAndEvaluation	  : [:], //ok
                lastSubmissions                           : [], //ok
                lastCorrectSubmissions                    : [], //ok
                problemHistory                            : [:], //ok
                ndCountHistory				              : [:], //ok
                solvedProblemsCountByNd                   : [:], //ok
                triedProblemsCountByNd                    : [:], //ok
                triedProblemsCountByTopic                 : [:], //ok
                ndCount                                   : [:]
        ]

        extractSubmissionsData(submissions, submissionsData)
        return submissionsData
    }

    def groupContext(Submission submission) {
        return groupContext([submission])
    }
    def groupContext(List<Submission> submissions) {

        def submissionsData = [
                submissionsCount              : 0, //Ok
//                usersWhoTried                 : [],//Ok
//                usersWhoSolved                : [], //Ok
//                triedProblems   			  : [], //Ok
//                solvedProblems                : [], //Ok
                submissionsCountByLanguageAndEvaluation    : mapCountByLanguageAndEvaluation(), //Ok
                submissionsCountByEvaluation  : mapCountByEvaluation(), //Ok
                submissionsCountByUser        : [:],//Ok
                userTriedTopicsCount          : [:], //Ok
                userSolvedTopicsCount         : [:], //Ok
//                solvedProblemsCountByTopic    : [:], //Ok
//                solvedProblemsCountByNd		  : [:], //Ok
                submissionsCountHistoryByEvaluation : [:], //Ok
                submissionsCountByProblemAndEvaluation : [:], //Ok
                problemsTriedByUser        : [:], //Ok
                problemHistory                : [:], //Consertar topic info
                problemsSolvedByUser       : [:], //Ok
                lastSubmissions               : [], //ok
                lastCorrectSubmissions        : [], //ok
                fastestSubmissions            : [], //ok
//                ndCountHistory				  : [:], //Ok
                usersWhoTriedCountHistory     : [:] //Ok
//                openQuizzes                   : [],
//                closedQuizzes                 : [],
//                quizzes                       : [],
//                accessCountHistory            : accessCountHistory,
//                openQuizzesHistory            : [:]


        ]
        extractSubmissionsData(submissions, submissionsData)

        return submissionsData
    }

    def quizContext(Submission submission) {
        quizContext([submission])
    }
    def quizContext(List<Submission> submissions) {
        def submissionsData = [
                submissionsCount              : 0, //Ok
                usersWhoTried                 : [],//Ok
                usersWhoSolved                : [], //Ok
                triedProblems   			  : [], //Ok
                solvedProblems                : [], //Ok
                submissionsCountByLanguageAndEvaluation    : mapCountByLanguageAndEvaluation(), //Ok
                submissionsCountByEvaluation  : mapCountByEvaluation(), //Ok
                submissionsCountByUser        : [:],//Ok
                userTriedTopicsCount          : [:], //Ok
                userSolvedTopicsCount         : [:], //Ok
                solvedProblemsCountByTopic    : [:], //Ok
                solvedProblemsCountByNd		  : [:], //Ok
                submissionsCountHistoryByEvaluation : [:], //Ok
                submissionsCountByProblemAndEvaluation : [:], //Ok
                problemsTriedByUser        : [:], //Ok
                problemHistory                : [:], //Consertar topic info
                usersWhoSolvedByProblem       : [:], //Ok
                problemsSolvedByUser          : [:],
                problemsTriedByUser           : [:],
                lastSubmissions               : [], //ok
                lastCorrectSubmissions        : [], //ok
                fastestSubmissions            : [], //ok
                ndCountHistory				  : [:], //Ok
                usersWhoTriedCountHistory     : [:] //Ok
//                usersScores				      : usersScores
        ]
        extractSubmissionsData(submissions, submissionsData)

        return submissionsData
    }


    def getQuizStatistic(quizId) {
        Statistic statistic = statisticRepository.findByIdAndEntity(quizId, Statistic.Entity.QUIZ)
        def statisticMap
        def headers = RestUtils.getAuthorizationHeaders(userLogin, passwordLogin)
        def userList = userByQuiz(quizId, headers)
        def problemList = problemByQuiz(quizId, headers)
        if (!statistic) {
            try {
                RestUtils.getRestClient().get(path: "quizzes/$quizId", headers: headers) {
                    response, json ->
                        statistic = new Statistic()
                        statistic.users = userList*.id
                        statistic.problems = problemList*.id
                        statistic.startDate = formatter.parseDateTime(json.startDate as String).toDate()
                        statistic.endDate = formatter.parseDateTime(json.endDate as String).toDate()
                        statistic.id = quizId
                        statistic.entity = Statistic.Entity.QUIZ
                        def submissionList = submissionRepository.findAllByUserIdInAndProblemIdInAndSubmissionDateBetween(statistic.users, statistic.problems, statistic.startDate, statistic.endDate)
                        statisticMap = quizContext(submissionList)
                        def lastUpdated = today
                        if (submissionList.size() > 0) {
                            lastUpdated = submissionList.sort { a, b -> a.submissionDate == b.submissionDate ? 0 : a.submissionDate < b.submissionDate ? -1 : 1 }[0].submissionDate
                        }

                        statistic.lasUpdated = lastUpdated
                        statistic.statistics = statisticMap
                        if (!statisticRepository.findByIdAndEntity(quizId, Statistic.Entity.QUIZ)) {
                            statisticRepository.save(statistic)
                        }

                }
            } catch (e) {
                println e.message
                println e.stackTrace
                e.finalize()
                if (e.message.equals("Not Found")) {
                    return false
                }
            }
        }
        statisticMap = statistic.statistics
        statisticMap.usersScores = userList
        statisticMap.problemList = problemList
        return statisticMap
    }

    def getUserStatistic(long userId) {
        Statistic statistic = statisticRepository.findByIdAndEntity(userId, Statistic.Entity.USER)
        def statisticMap
        if (!statistic) {
            def submissionList = submissionRepository.findAllByUserId(userId)
            statisticMap = userContext(submissionList)
            def lastUpdated = today
            if (submissionList.size() > 0) {
                lastUpdated = submissionList.sort { a, b -> a.submissionDate == b.submissionDate ? 0 : a.submissionDate < b.submissionDate ? -1 : 1 }[0].submissionDate
            }

            statistic = new Statistic()
            statistic.id = userId
            statistic.lasUpdated = lastUpdated
            statistic.statistics = statisticMap
            statistic.entity = Statistic.Entity.USER
            if (!statisticRepository.findByIdAndEntity(userId, Statistic.Entity.USER)) {
                statisticRepository.save(statistic)
            }
        }

        return statistic.statistics
    }

    def getProblemStatistic(long problemId) {
        Statistic statistic = statisticRepository.findByIdAndEntity(problemId, Statistic.Entity.PROBLEM)
        def statisticMap
        if (!statistic) {
            updateProblem(problemId)
            def submissionList = submissionRepository.findAllByProblemId(problemId)
            statisticMap = extractSubmissionsData(submissionList)
            def lastUpdated = today
            if (submissionList.size() > 0) {
                lastUpdated = submissionList.sort { a, b -> a.submissionDate == b.submissionDate ? 0 : a.submissionDate < b.submissionDate ? -1 : 1 }[0].submissionDate
            }
            statistic = new Statistic()
            statistic.id = problemId
            statistic.lasUpdated = lastUpdated
            statistic.statistics = statisticMap
            statistic.entity = Statistic.Entity.PROBLEM
            if (!statisticRepository.findByIdAndEntity(problemId, Statistic.Entity.PROBLEM)) {
                statisticRepository.save(statistic)
            }
        }

        return statistic.statistics
    }

    def getGroupStatistic(long groupId) {
        Statistic statistic = statisticRepository.findByIdAndEntity(groupId, Statistic.Entity.GROUP)
        def statisticMap
        def userMap = userByRoleInGroup(groupId)
        if (!statistic) {
            try {
                RestUtils.getRestClient().get(path: "groups/$groupId") { response, json ->
                    statistic = new Statistic()
                    statistic.users = userMap[User.Role.STUDENT]*.id
                    statistic.startDate = formatter.parseDateTime(json.startDate as String).toDate()
                    statistic.endDate = formatter.parseDateTime(json.endDate as String).toDate()
                    statistic.id = groupId
                    statistic.entity = Statistic.Entity.GROUP

                    def submissionList = submissionRepository.findAllByUserIdInAndSubmissionDateBetween(statistic.users, statistic.startDate, statistic.endDate)
                    statisticMap = groupContext(submissionList)
                    def lastUpdated = today
                    if (submissionList.size() > 0) {
                        lastUpdated = submissionList.sort { a, b -> a.submissionDate == b.submissionDate ? 0 : a.submissionDate < b.submissionDate ? -1 : 1 }[0].submissionDate
                    }

                    statistic.lasUpdated = lastUpdated
                    statistic.statistics = statisticMap
                    if (!statisticRepository.findByIdAndEntity(groupId, Statistic.Entity.GROUP)) {
                        statisticRepository.save(statistic)
                    }


                }
            } catch (e) {
                println e.message
                println e.stackTrace
                e.finalize()
                if (e.message.equals("Not Found")) {
                    return false
                }
            }

        }

        statisticMap = statistic.statistics
        statisticMap.closedquizzes = []
        statisticMap.openquizzes = []
        statisticMap.openQuizzesHistory = [:]

        statisticMap.student = userMap[User.Role.STUDENT]
        statisticMap.teacher = userMap[User.Role.TEACHER]
        statisticMap.teacherAssistant = userMap[User.Role.TEACHER_ASSISTANT]
        statisticMap.quizzes = quizByGroup(groupId)
        statisticMap.quizzes.each() { quiz ->
            def startDate = formatter.parseDateTime(quiz.startDate as String).toDate()
            def endDate = formatter.parseDateTime(quiz.endDate as String).toDate()
            if (endDate > today) {
                if (startDate < today) {
                    statisticMap.openquizzes.add(quiz)
                }
            } else {
                statisticMap.closedquizzes.add(quiz)
            }
            (0..(endDate - startDate)).each {
                def key = new DateTime(startDate.plus(it as Integer)).withTimeAtStartOfDay().toString(formatter)
                if (!statisticMap.openQuizzesHistory.containsKey(key)) {
                    statisticMap.openQuizzesHistory[key] = []
                }
                statisticMap.openQuizzesHistory[key].add(quiz)
            }
        }



        return statisticMap
    }

    def userByRoleInGroup(groupId) {
        def count = 0
        def more = true
        def total = 0
        def responseMap = [:]
        User.Role.each() { role ->
            responseMap[role] = []
        }

        while(more) {

            def inc = count++ * 100

            try {
                RestUtils.getRestClient().get(path: "groups/$groupId/users", query: [max: 100, offset: inc, sort: 'name', order: 'asc']) { response, json ->
                    more = !json.empty

                    total = response.headers['total'].value as Long

                    json.each { user ->
                        responseMap[User.Role.valueOf(user.role as String)].add(updateUser(user))
                    }
                }
            } catch (e) {
                println e.message
                println e.stackTrace
                e.finalize()
                if (e.message.equals("Not Found")) {
                    return responseMap
                }
            }
        }
        return responseMap
    }

    def quizByGroup(groupId) {
        def count = 0
        def more = true
        def total = 0
        def responseList = []

        while(more) {

            def inc = count++ * 100

            try {
                RestUtils.getRestClient().get(path: "groups/$groupId/quizzes", query: [max: 100, offset: inc, sort: 'endDate', order: 'asc']) { response, json ->

                    more = !json.empty

                    total = response.headers['total'].value as Long

                    json.each { quiz ->
                        responseList.add(quiz)
                    }
                }
            } catch (e) {
                println e.message
                println e.stackTrace
                e.finalize()
                if (e.message.equals("Not Found")) {
                    return responseList
                }
            }
        }
        return responseList
    }

    def userByQuiz(quizId, headers) {
        def count = 0
        def more = true
        def total = 0
        def responseList = []

        while(more) {

            def inc = count++ * 100

            try {
                RestUtils.getRestClient().get(path: "quizzes/$quizId/users", headers: headers, query: [max: 100, offset: inc, sort: 'name', order: 'asc']) { response, json ->
                    more = !json.empty

                    total = response.headers['total'].value as Long

                    json.each { user ->
                        responseList.add(updateUser(user))
                    }
                }
            } catch (e) {
                println e.message
                println e.stackTrace
                e.finalize()
                if (e.message.equals("Not Found")) {
                    return responseList
                }
            }
        }
        return responseList
    }

    def problemByQuiz(quizId, headers) {
        def count = 0
        def more = true
        def total = 0
        def responseList = []

        while(more) {

            def inc = count++ * 100

            try {
                RestUtils.getRestClient().get(path: "quizzes/$quizId/problems", headers: headers, query: [max: 100, offset: inc, sort: 'name', order: 'asc']) { response, json ->

                    more = !json.empty

                    total = response.headers['total'].value as Long

                    json.each { problem ->
                        def tempProblem = updateProblem(problem)
                        tempProblem.nd = problem.nd
                        responseList.add(tempProblem)
                    }
                }
            } catch (e) {
                println e.message
                println e.stackTrace
                e.finalize()
                if (e.message.equals("Not Found")) {
                    return responseList
                }
            }
        }
        return responseList
    }

    def updateUser(json) {
        def responseUser = [id: json.id, name: json.name, avatar: json.avatar]
        User user = userRepository.findById(json.id as Long)
        if (!user) {
            user = new User()
            user.id = json.id
        }

        user.name = json.name
        user.avatar = json.avatar
        userRepository.save(user)
        if (json.quiz != null && json.quiz.score != null) {
            responseUser.score = json.quiz.score
            return responseUser
        }
        return user

    }

    def updateProblem(json) {
        def responseProblem = [id: json.id, name: json.name]
        Problem problem = problemRepository.findById(json.id as Long)
        if (!problem) {
            problem = new Problem()
            problem.id = json.id
        }
        problem.name = json.name
        problem.nd = json.nd
        problem.topics = []

        json.topics.each { t ->
            def topic = topicRepository.findById(t.id as Long)

            if (!topic) {
                topic = new Topic(id: t.id)
            }

            topic.name = t.name
            topicRepository.save(topic)

            problem.topics.add(topic)
        }

        problemRepository.save(problem)

        if (json.score != null) {
            responseProblem.score = json.score
            responseProblem.topics = problem.topics
            return responseProblem
        }
        return problem

    }

    def updateUser(long id) {
        try {
            RestUtils.getRestClient().get(path: "users/$id") { response, json ->
                updateUser(json)
            }
        } catch (e) {
            println e.message
            e.printStackTrace()
            e.finalize()
        }
    }
    def updateProblem(long id) {
        try {
            RestUtils.getRestClient().get(path: "problems/$id") { response, json ->
                updateProblem(json)
            }
        } catch (e) {
            println e.message
            e.printStackTrace()
            e.finalize()
        }
    }
}
