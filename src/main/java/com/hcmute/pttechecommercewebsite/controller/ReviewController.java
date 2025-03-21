package com.hcmute.pttechecommercewebsite.controller;

import com.hcmute.pttechecommercewebsite.dto.ReviewDTO;
import com.hcmute.pttechecommercewebsite.model.Review;
import com.hcmute.pttechecommercewebsite.service.ReviewService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Xem tất cả đánh giá chưa bị xóa
    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<ReviewDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    // Xem đánh giá theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable String id) {
        Optional<ReviewDTO> review = reviewService.getReviewById(id);
        return review.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Xem tất cả đánh giá của người dùng
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUserId(@PathVariable String userId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByUserId(new ObjectId(userId));
        return ResponseEntity.ok(reviews);
    }

    // Xem tất cả đánh giá của sản phẩm
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByProductId(@PathVariable String productId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByProductId(new ObjectId(productId));
        return ResponseEntity.ok(reviews);
    }

    // Xem tất cả đánh giá của đơn hàng
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByOrderId(@PathVariable String orderId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByOrderId(new ObjectId(orderId));
        return ResponseEntity.ok(reviews);
    }

    // Thêm đánh giá mới
    @PostMapping
    public ResponseEntity<ReviewDTO> addReview(@RequestBody Review review) {
        ReviewDTO createdReview = reviewService.addReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    // Chỉnh sửa đánh giá
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(@PathVariable String id, @RequestBody Review review) {
        Optional<ReviewDTO> updatedReview = reviewService.updateReview(id, review);
        return updatedReview.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Xóa mềm đánh giá
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        boolean isDeleted = reviewService.deleteReview(id);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Trả lời đánh giá
    @PostMapping("/reply/{id}")
    public ResponseEntity<ReviewDTO> replyToReview(@PathVariable String id, @RequestParam("replyText") String replyText) {
        Optional<ReviewDTO> reviewWithReply = reviewService.replyToReview(id, replyText);
        return reviewWithReply.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}