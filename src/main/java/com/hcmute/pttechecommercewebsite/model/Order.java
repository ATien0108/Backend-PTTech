package com.hcmute.pttechecommercewebsite.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "Orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    private String id;                            // ID duy nhất của đơn hàng.

    private String orderId;                       // ID đơn hàng duy nhất.

    private ObjectId userId;                      // ID tham chiếu đến người dùng (người đặt hàng).

    private List<Item> items;                     // Danh sách các sản phẩm trong đơn hàng.

    private int totalItems;                       // Tổng số sản phẩm trong đơn hàng.

    private double totalPrice;                    // Tổng giá trị của đơn hàng.

    private double shippingPrice;                 // Giá vận chuyển.

    private String discountCode;                  // Mã giảm giá.

    private double discountAmount;                // Số tiền giảm giá từ mã giảm giá.

    private double finalPrice;                    // Tổng giá trị đơn hàng sau khi áp dụng giảm giá và cộng phí vận chuyển.

    private String phoneNumber;                   // Số điện thoại của người nhận.

    private ShippingAddress shippingAddress;      // Địa chỉ giao hàng.

    private String paymentMethod;                 // Phương thức thanh toán.

    private String paymentStatus;                 // Trạng thái thanh toán.

    private String orderStatus;                   // Trạng thái đơn hàng.

    private String shippingMethod;                // Phương thức giao hàng.

    @CreatedDate
    private Date createdAt;                       // Thời gian tạo đơn hàng.

    @LastModifiedDate
    private Date updatedAt;                       // Thời gian cập nhật đơn hàng.

    private boolean isDeleted;                    // Trạng thái xóa mềm.

    private String orderNotes;                    // Các ghi chú về đơn hàng.

    private String cancellationReason;            // lý do hủy

    private String returnReason;

    private boolean isReturnApproved;             // Kiểm tra yêu cầu trả hàng đã được duyệt chưa.

    private String returnRejectionReason;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private ObjectId productId;                // ID tham chiếu đến sản phẩm.
        private ObjectId variantId;                // ID tham chiếu đến biến thể sản phẩm.
        private ObjectId brandId;                  // ID thương hiệu.
        private ObjectId categoryId;               // ID danh mục sản phẩm.
        private double discountPrice;               // Giá giảm của sản phẩm.
        private double originalPrice;               // Giá gốc của sản phẩm.
        private int quantity;                       // Số lượng sản phẩm trong đơn hàng.
        private double totalPrice;                  // Tổng giá của sản phẩm trong đơn hàng.

        private String productName;                 // Tên sản phẩm.
        private String color;                       // Màu sắc của sản phẩm.
        private String hexCode;                     // Mã màu HEX của sản phẩm.
        private String size;                        // Kích thước của biến thể sản phẩm.
        private String ram;                         // RAM của biến thể.
        private String storage;                     // Storage của biến thể.
        private String condition;                   // Tình trạng của biến thể.
        private String productImage;                // Hình ảnh của sản phẩm.

        @CreatedDate
        private Date createdAt;                     // Ngày sản phẩm được thêm vào đơn hàng.

        @LastModifiedDate
        private Date updatedAt;                     // Ngày sản phẩm được cập nhật trong đơn hàng.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingAddress {
        private String street;                      // Địa chỉ đường phố.
        private String communes;                    // Xã/phường.
        private String district;                    // Quận/huyện.
        private String city;                        // Thành phố.
        private String country;                     // Quốc gia.
    }
}