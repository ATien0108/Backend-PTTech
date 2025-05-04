package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.Brand;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends MongoRepository<Brand, String> {

    // Tìm thương hiệu theo ID
    Optional<Brand> findById(String id);

    // Tìm tất cả thương hiệu mà không bị xóa (isDeleted == false) và hiển thị (isVisible == true)
    List<Brand> findByIsDeletedFalseAndIsActiveTrue(Sort sort);

    // Tìm tất cả thương hiệu mà không bị xóa (isDeleted == false)
    List<Brand> findByIsDeletedFalse(Sort sort);

    Optional<Brand> findByIdAndIsDeletedFalse(String id);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isDeleted': false, 'isVisible': true }")
    List<Brand> findByNameContaining(String keyword);

    // Tìm tất cả thương hiệu có thời gian lên lịch nhỏ hơn hoặc bằng thời gian hiện tại và chưa kích hoạt
    List<Brand> findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(Date now);

    List<Brand> findByNameInIgnoreCase(List<String> names);
}