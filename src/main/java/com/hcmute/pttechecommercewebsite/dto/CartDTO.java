package com.hcmute.pttechecommercewebsite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {

    private String id;                            // ID duy nhất của giỏ hàng.

    private String userId;                        // ID tham chiếu đến người dùng.

    private List<ItemDTO> items;                  // Danh sách các sản phẩm trong giỏ hàng.

    private int totalItems;                       // Tổng số sản phẩm trong giỏ hàng.
    private double totalPrice;                    // Tổng giá trị của tất cả các sản phẩm trong giỏ hàng.

    @JsonProperty("isDeleted")
    private boolean isDeleted;                    // Trạng thái xóa mềm (true: đã xóa, false: chưa xóa).

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDTO {
        private String productId;                  // ID tham chiếu đến sản phẩm.
        private String variantId;                  // ID tham chiếu đến biến thể sản phẩm (nếu có).
        private String brandId;                   // ID thương hiệu.
        private String categoryId;                // ID danh mục sản phẩm.
        private int quantity;                      // Số lượng sản phẩm trong giỏ hàng.
        private double totalPrice;                      // Tổng giá cho sản phẩm (quantity * price).

        // Các thông tin chi tiết về sản phẩm
        private String productName;                // Tên sản phẩm.
        private double originalPrice;              // Giá gốc của sản phẩm.
        private double discountPrice;              // Giá giảm của sản phẩm (nếu có).
        private double ratingAverage;       // Điểm đánh giá trung bình.
        private int totalReviews;           // Tổng số đánh giá.
        private String productImage;        // Danh sách hình ảnh của sản phẩm.

        // Các thông tin chi tiết về biến thể
        private String color;               // Màu sắc của biến thể.
        private String hexCode;             // Mã màu HEX của biến thể.
        private String size;                // Kích thước của biến thể.
        private String ram;                 // RAM của biến thể.
        private String storage;             // Storage của biến thể.
        private String condition;           // Tình trạng của biến thể (mới, cũ, refurbished).

        private Date createdAt;                    // Ngày sản phẩm được thêm vào giỏ hàng.
        private Date updatedAt;                    // Ngày sản phẩm được cập nhật trong giỏ hàng.
    }
}