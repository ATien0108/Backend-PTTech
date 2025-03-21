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
public class OrderDTO {

    private String id;                            // ID duy nhất của đơn hàng.

    private String orderId;                       // ID đơn hàng duy nhất.

    private String userId;                        // ID tham chiếu đến người dùng (người đặt hàng).

    private List<ItemDTO> items;                  // Danh sách các sản phẩm trong đơn hàng.

    private int totalItems;                       // Tổng số sản phẩm trong đơn hàng.

    private double totalPrice;                    // Tổng giá trị của đơn hàng.

    private double shippingPrice;                 // Giá vận chuyển.

    private String discountCode;                  // Mã giảm giá.

    private double discountAmount;                // Số tiền giảm giá từ mã giảm giá.

    private double finalPrice;                    // Tổng giá trị đơn hàng sau khi áp dụng giảm giá và cộng phí vận chuyển.

    private String phoneNumber;                   // Số điện thoại của người nhận.

    private ShippingAddressDTO shippingAddress;   // Địa chỉ giao hàng.

    private String paymentMethod;                 // Phương thức thanh toán.

    private String paymentStatus;                 // Trạng thái thanh toán.

    private String orderStatus;                   // Trạng thái đơn hàng.

    private String shippingMethod;                // Phương thức giao hàng.

    private Date createdAt;                       // Thời gian tạo đơn hàng.

    private Date updatedAt;                       // Thời gian cập nhật đơn hàng.

    @JsonProperty("isDeleted")
    private boolean isDeleted;                    // Trạng thái xóa mềm.

    private String orderNotes;                    // Các ghi chú về đơn hàng.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDTO {
        private String productId;                  // ID tham chiếu đến sản phẩm.
        private String variantId;                  // ID tham chiếu đến biến thể sản phẩm.
        private String brandId;                    // ID thương hiệu.
        private String categoryId;                 // ID danh mục sản phẩm.
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

        private Date createdAt;                     // Ngày sản phẩm được thêm vào đơn hàng.

        private Date updatedAt;                     // Ngày sản phẩm được cập nhật trong đơn hàng.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingAddressDTO {
        private String street;                      // Địa chỉ đường phố.
        private String communes;                    // Xã/phường.
        private String district;                    // Quận/huyện.
        private String city;                        // Thành phố.
        private String country;                     // Quốc gia.
    }
}