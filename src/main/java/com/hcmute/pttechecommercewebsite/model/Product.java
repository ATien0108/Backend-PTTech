package com.hcmute.pttechecommercewebsite.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private String id;                       // ID duy nhất của sản phẩm.

    @NotNull(message = "Mã sản phẩm không được để trống")
    private String productId;                 // ID sản phẩm duy nhất.

    @NotNull(message = "Tên sản phẩm không được để trống")
    private String name;                     // Tên sản phẩm.

    private ObjectId brandId;                // ID tham chiếu đến document 'brands' (ObjectId).
    private ObjectId categoryId;             // ID tham chiếu đến document 'categories' (ObjectId).

    @NotNull(message = "Mô tả sản phẩm không được để trống")
    private String description;              // Mô tả chi tiết về sản phẩm.

    private Pricing pricing;                 // Thông tin về giá cả sản phẩm.
    private Map<String, String> specifications; // Các thông số kỹ thuật của sản phẩm.
    private List<Variant> variants;          // Danh sách các biến thể sản phẩm.

    private List<String> tags;               // Danh sách các tag sản phẩm.
    private List<String> images;             // Danh sách hình ảnh của sản phẩm.
    private List<String> videos;             // Các video liên quan đến sản phẩm.

    private Blog blog;                       // Thông tin bài viết blog liên quan đến sản phẩm.
    private Ratings ratings;                 // Đánh giá sản phẩm.
    private Warranty warranty;               // Thông tin bảo hành sản phẩm.

    private int totalSold;                   // Tổng số lượng sản phẩm đã bán.

    private String status;                   // Trạng thái sản phẩm (ví dụ: "active").
    private String visibilityType;           // Loại hiển thị sản phẩm.

    private boolean isDeleted;               // Trạng thái xóa mềm.

    private Date scheduledDate;

    @CreatedDate
    private Date createdAt;                  // Ngày tạo sản phẩm.

    @LastModifiedDate
    private Date updatedAt;                  // Ngày cập nhật sản phẩm.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Pricing {
        private double original;              // Giá gốc ban đầu của sản phẩm.
        private double current;               // Giá hiện tại của sản phẩm.
        private List<PriceHistory> history;   // Lịch sử thay đổi giá.

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PriceHistory {
            private double previousPrice;     // Giá trước khi thay đổi.
            private double newPrice;          // Giá mới sau khi thay đổi.
            private Date changedAt;           // Thời điểm thay đổi giá.
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Variant {
        private ObjectId variantId;          // ID biến thể (ObjectId).
        private String color;                // Màu sắc của biến thể.
        private String hexCode;              // Mã màu HEX.
        private String size;                 // Kích thước của biến thể.
        private String ram;                  // RAM của biến thể.
        private String storage;              // Storage của biến thể.
        private String condition;            // Tình trạng của biến thể.
        private int stock;                   // Số lượng tồn kho.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Blog {
        private String title;                // Tiêu đề bài viết.
        private String description;          // Tóm tắt bài viết.
        private String content;              // Nội dung bài viết.
        private Date publishedDate;          // Ngày xuất bản bài viết.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Ratings {
        private double average;              // Điểm trung bình của các đánh giá.
        private int totalReviews;            // Tổng số lượt đánh giá.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Warranty {
        private String duration;             // Thời hạn bảo hành.
        private String terms;                // Điều khoản bảo hành.
    }
}