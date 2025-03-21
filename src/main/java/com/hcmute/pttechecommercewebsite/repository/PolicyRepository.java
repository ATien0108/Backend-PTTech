package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.Policy;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends MongoRepository<Policy, String> {

    Optional<Policy> findById(String id);

    List<Policy> findByIsDeletedFalseAndIsActiveTrue(Sort sort);

    List<Policy> findByIsDeletedFalse(Sort sort);

    @Query("{ 'title': { $regex: ?0, $options: 'i' }, 'isDeleted': false }")
    List<Policy> findByTitleContaining(String keyword);

    // Tìm tất cả chính sách có thời gian lên lịch nhỏ hơn hoặc bằng thời gian hiện tại và chưa kích hoạt
    List<Policy> findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(Date now);
}