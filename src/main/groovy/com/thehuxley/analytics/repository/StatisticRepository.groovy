package com.thehuxley.analytics.repository

import com.thehuxley.analytics.domain.Statistic
import com.thehuxley.analytics.domain.Statistic.Entity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StatisticRepository extends MongoRepository<Statistic, String> {
    Statistic findById(Long id)

    Statistic findByIdAndEntity(Long id, Entity entity)

    List<Statistic> findAllByUsersAndStartDateGreaterThanEqualAndEndDateLessThanEqualAndEntity(Long userId, Date submissionDate, Date submissionDate2 , Entity entity)

    List<Statistic> findAllByUsersAndProblemsAndStartDateGreaterThanEqualAndEndDateLessThanEqualAndEntity(Long userId, Long problemId, Date submissionDate, Date submissionDate2 , Entity entity)

}
