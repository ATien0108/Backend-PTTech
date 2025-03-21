package com.hcmute.pttechecommercewebsite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private String id;                       // ID duy nhất của sản phẩm.
    private String productId;                 // ID sản phẩm duy nhất.
    private String name;                     // Tên sản phẩm.
    private String brandId;                  // ID tham chiếu đến document 'brands'.
    private String categoryId;               // ID tham chiếu đến document 'categories'.
    private String description;              // Mô tả sản phẩm.
    private PricingDTO pricing;              // Thông tin về giá sản phẩm.
    private Map<String, String> specifications; // Các thông số kỹ thuật.
    private List<VariantDTO> variants;       // Danh sách các biến thể sản phẩm.
    private List<String> tags;               // Danh sách các tag.
    private List<String> images;             // Danh sách hình ảnh của sản phẩm.
    private List<String> videos;             // Danh sách video sản phẩm.
    private BlogDTO blog;                    // Thông tin bài viết blog.
    private RatingsDTO ratings;              // Đánh giá sản phẩm.
    private WarrantyDTO warranty;            // Thông tin bảo hành.
    private int totalSold;                   // Tổng số lượng sản phẩm đã bán.
    private String status;                   // Trạng thái sản phẩm.
    private String visibilityType;           // Loại hiển thị sản phẩm.
    @JsonProperty("isDeleted")
    private boolean isDeleted;               // Trạng thái xóa mềm.

    private Date scheduledDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PricingDTO {
        private double original;              // Giá gốc.
        private double current;               // Giá hiện tại.
        private List<PriceHistoryDTO> history; // Lịch sử thay đổi giá.

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PriceHistoryDTO {
            private double previousPrice;     // Giá trước khi thay đổi.
            private double newPrice;          // Giá mới.
            private Date changedAt;           // Ngày thay đổi.
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VariantDTO {
        private String variantId;            // ID biến thể.
        private String color;                // Màu sắc.
        private String hexCode;              // Mã màu HEX.
        private String size;                 // Kích thước.
        private String ram;                  // RAM.
        private String storage;              // Storage.
        private String condition;            // Tình trạng.
        private int stock;                   // Số lượng tồn kho.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BlogDTO {
        private String title;                // Tiêu đề bài viết.
        private String description;          // Tóm tắt bài viết.
        private String content;              // Nội dung bài viết.
        private Date publishedDate;          // Ngày xuất bản.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RatingsDTO {
        private double average;              // Điểm trung bình.
        private int totalReviews;            // Tổng số đánh giá.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarrantyDTO {
        private String duration;             // Thời hạn bảo hành.
        private String terms;                // Điều khoản bảo hành.
    }
}