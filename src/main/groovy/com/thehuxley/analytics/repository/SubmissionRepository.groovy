package com.thehuxley.analytics.repository

import com.thehuxley.analytics.domain.Submission
import com.thehuxley.analytics.repository.mongodb.SubmissionRepositoryCustom
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubmissionRepository extends MongoRepository<Submission, String>, SubmissionRepositoryCustom {

    Submission findById(Long id)

    List<Submission> findAllByUserId(Long id)

    List<Submission> findAllByProblemId(Long id)

    List<Submission> findAllByUserIdInAndProblemIdInAndSubmissionDateBetween(List<Long> userIdList, List<Long> problemIdList, Date startDate, Date endDate)

    List<Submission> findAllByUserIdInAndSubmissionDateBetween(List<Long> userIdList, Date startDate, Date endDate)


}
