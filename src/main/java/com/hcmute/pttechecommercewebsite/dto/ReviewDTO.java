package com.hcmute.pttechecommercewebsite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {

    private String id;                           // ID duy nhất của đánh giá.

    private String productId;                    // ID tham chiếu đến sản phẩm.
    private String productVariantId;             // ID tham chiếu đến biến thể sản phẩm (nếu có).
    private String userId;                       // ID tham chiếu đến người dùng đã đánh giá.
    private String orderId;                      // ID tham chiếu đến đơn hàng.

    private double rating;                       // Điểm đánh giá (ví dụ: từ 1 đến 5 sao).
    private String review;                       // Nội dung đánh giá của người dùng.
    private String reviewTitle;                  // Tiêu đề ngắn gọn cho đánh giá.

    private List<String> images;                 // Các hình ảnh người dùng có thể đính kèm.

    private ReplyDTO reply;                      // Trả lời của người bán hàng (nếu có).

    @JsonProperty("isDeleted")
    private boolean isDeleted;                   // Đánh dấu đánh giá đã bị xóa (true: đã xóa, false: chưa xóa).

    private Date createdAt;                      // Ngày người dùng để lại đánh giá.
    private Date updatedAt;                      // Ngày cập nhật đánh giá (nếu có thay đổi).

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReplyDTO {
        private String replyText;                // Nội dung trả lời từ người bán hàng.
        private Date replyDate;                  // Ngày trả lời.
    }
}