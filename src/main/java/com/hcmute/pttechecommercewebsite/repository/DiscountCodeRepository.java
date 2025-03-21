package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.DiscountCode;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountCodeRepository extends MongoRepository<DiscountCode, String> {

    // Tìm mã giảm giá theo ID
    Optional<DiscountCode> findById(String id);

    // Tìm tất cả mã giảm giá đang hoạt động và không bị xóa
    List<DiscountCode> findByIsActiveTrueAndIsDeletedFalse(Sort sort);

    // Tìm tất cả mã giảm giá không bị xóa
    List<DiscountCode> findByIsDeletedFalse(Sort sort);

    // Tìm mã giảm giá theo code (không phân biệt chữ hoa/thường)
    @Query("{ 'code': { $regex: ?0, $options: 'i' }, 'isDeleted': false }")
    List<DiscountCode> findByCodeContaining(String keyword);

    // Tìm tất cả mã giảm giá có thời gian lên lịch nhỏ hơn hoặc bằng thời gian hiện tại và chưa kích hoạt
    List<DiscountCode> findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(Date now);

    // Tìm mã giảm giá theo code và trạng thái hoạt động, chưa xóa, và còn trong thời gian hiệu lực
    @Query("{ 'code': ?0, 'isDeleted': false, 'isActive': true, 'startDate': { $lte: ?1 }, 'endDate': { $gte: ?1 } }")
    Optional<DiscountCode> findByCodeAndIsActiveTrueAndIsDeletedFalseAndValidDateRange(String discountCode, Date currentDate);
}