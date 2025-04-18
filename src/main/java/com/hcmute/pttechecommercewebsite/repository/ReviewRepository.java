package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.Review;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByIsDeletedFalse();  // Lấy tất cả đánh giá chưa bị xóa.

    List<Review> findByUserIdAndIsDeletedFalse(ObjectId userId);  // Lấy tất cả đánh giá của người dùng.

    List<Review> findByProductIdAndIsDeletedFalse(ObjectId productId);  // Lấy tất cả đánh giá của sản phẩm.

    List<Review> findByOrderIdAndIsDeletedFalse(ObjectId orderId);  // Lấy tất cả đánh giá của đơn hàng.

    Optional<Review> findByIdAndIsDeletedFalse(String id);  // Lấy đánh giá theo ID và chưa bị xóa.

    // Phương thức tìm đánh giá trong khoảng thời gian
    List<Review> findByCreatedAtBetween(Date startDate, Date endDate);
}
