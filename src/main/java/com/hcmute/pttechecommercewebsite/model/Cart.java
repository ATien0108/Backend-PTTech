package com.hcmute.pttechecommercewebsite.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "Carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    private String id;                            // ID duy nhất của giỏ hàng.

    private ObjectId userId;                      // ID tham chiếu đến người dùng.

    private List<Item> items;                     // Danh sách các sản phẩm trong giỏ hàng.

    private int totalItems;                       // Tổng số sản phẩm trong giỏ hàng.
    private double totalPrice;                    // Tổng giá trị của tất cả các sản phẩm trong giỏ hàng.

    private boolean isDeleted;                    // Trạng thái xóa mềm (true: đã xóa, false: chưa xóa).

    @CreatedDate
    private Date createdAt;                       // Ngày tạo giỏ hàng.

    @LastModifiedDate
    private Date updatedAt;                       // Ngày cập nhật giỏ hàng lần cuối.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private ObjectId productId;                // ID tham chiếu đến sản phẩm.
        private ObjectId variantId;                // ID tham chiếu đến biến thể sản phẩm (nếu có).
        private ObjectId brandId;                   // ID thương hiệu.
        private ObjectId categoryId;                // ID danh mục sản phẩm.
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

        @CreatedDate
        private Date createdAt;                    // Ngày sản phẩm được thêm vào giỏ hàng.

        @LastModifiedDate
        private Date updatedAt;                    // Ngày sản phẩm được cập nhật trong giỏ hàng.
    }
}