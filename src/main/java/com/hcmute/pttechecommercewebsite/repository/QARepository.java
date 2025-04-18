package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.QA;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface QARepository extends MongoRepository<QA, String> {

    // Lấy tất cả QA theo userId
    List<QA> findByUserId(ObjectId userId);

    // Lấy tất cả QA theo productId
    List<QA> findByProductId(ObjectId productId);
}