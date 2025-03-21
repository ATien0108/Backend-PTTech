package com.hcmute.pttechecommercewebsite.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "Reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    private String id;                           // ID duy nhất của đánh giá.

    private ObjectId productId;                  // ID tham chiếu đến sản phẩm.
    private ObjectId productVariantId;           // ID tham chiếu đến biến thể sản phẩm (nếu có).
    private ObjectId userId;                     // ID tham chiếu đến người dùng đã đánh giá.
    private ObjectId orderId;                    // ID tham chiếu đến đơn hàng.

    private double rating;                       // Điểm đánh giá (ví dụ: từ 1 đến 5 sao).
    private String review;                       // Nội dung đánh giá của người dùng.
    private String reviewTitle;                  // Tiêu đề ngắn gọn cho đánh giá.

    private List<String> images;                 // Các hình ảnh người dùng có thể đính kèm.

    private Reply reply;                         // Trả lời của người bán hàng (nếu có).

    private boolean isDeleted;                   // Đánh dấu đánh giá đã bị xóa (true: đã xóa, false: chưa xóa).

    @CreatedDate
    private Date createdAt;                      // Ngày người dùng để lại đánh giá.

    @LastModifiedDate
    private Date updatedAt;                      // Ngày cập nhật đánh giá (nếu có thay đổi).

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Reply {
        private String replyText;                // Nội dung trả lời từ người bán hàng.
        private Date replyDate;                  // Ngày trả lời.
    }
}