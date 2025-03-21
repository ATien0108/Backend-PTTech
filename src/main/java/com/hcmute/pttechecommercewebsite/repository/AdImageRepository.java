package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.AdImage;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdImageRepository extends MongoRepository<AdImage, String> {

    // Tìm quảng cáo theo ID
    Optional<AdImage> findById(String id);

    // Tìm tất cả quảng cáo đang hoạt động và không bị xóa
    List<AdImage> findByIsActiveTrueAndIsDeletedFalse(Sort sort);

    // Tìm tất cả quảng cáo không bị xóa
    List<AdImage> findByIsDeletedFalse(Sort sort);

    Optional<AdImage> findByIdAndIsDeletedFalse(String id);

    // Tìm quảng cáo theo tiêu đề (không phân biệt chữ hoa/thường)
    List<AdImage> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title);

    // Tìm tất cả quảng cáo có thời gian lên lịch nhỏ hơn hoặc bằng thời gian hiện tại và chưa kích hoạt
    List<AdImage> findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(Date now);
}