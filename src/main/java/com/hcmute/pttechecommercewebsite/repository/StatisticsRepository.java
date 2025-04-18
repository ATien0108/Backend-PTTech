package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.Statistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface StatisticsRepository extends MongoRepository<Statistics, String> {
    // Tìm thống kê theo ngày (bỏ qua giờ, phút, giây)
    Optional<Statistics> findByDateBetween(Date startOfDay, Date endOfDay);
}
