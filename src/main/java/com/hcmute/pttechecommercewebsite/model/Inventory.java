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

@Document(collection = "Inventories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    private String id;                       // ID duy nhất của bản ghi nhập kho.

    @NotNull(message = "Danh sách sản phẩm không được để trống")
    private List<ProductEntry> products;     // Danh sách các sản phẩm được nhập kho.

    private Supplier supplier;               // Thông tin nhà cung cấp (nếu có).
    private double totalAmount;              // Tổng giá trị của tất cả các sản phẩm nhập kho.
    private int totalQuantity;               // Tổng số lượng sản phẩm nhập kho.
    private String notes;                    // Ghi chú về việc nhập kho.
    private Date receivedDate;               // Ngày nhập kho.

    private boolean isDeleted;

    @CreatedDate
    private Date createdAt;                  // Ngày tạo bản ghi nhập kho.

    @LastModifiedDate
    private Date updatedAt;                  // Ngày cập nhật bản ghi nhập kho lần cuối.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductEntry {
        private ObjectId productId;              // ID tham chiếu đến sản phẩm (ObjectId).
        private String productName;            // Tên sản phẩm.
        private List<ProductVariantEntry> productVariants; // Danh sách các biến thể sản phẩm.

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ProductVariantEntry {
            private ObjectId productVariantId;    // ID tham chiếu đến biến thể sản phẩm (ObjectId).
            private String color;               // Màu sắc của biến thể.
            private String size;                // Kích thước của biến thể.
            private String ram;                 // RAM của biến thể.
            private String storage;             // Storage của biến thể.
            private int quantity;               // Số lượng sản phẩm nhập kho cho biến thể này.
            private double unitPrice;           // Đơn giá mỗi sản phẩm của biến thể.
            private double totalValue;          // Tổng giá trị của số lượng sản phẩm nhập kho (unitPrice * quantity).
            private int stockBeforeUpdate;      // Số lượng tồn kho trước khi nhập kho.
            private int stockAfterUpdate;       // Số lượng tồn kho sau khi nhập kho.
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Supplier {
        private String name;                  // Tên nhà cung cấp.
        private String contact;               // Thông tin liên hệ của nhà cung cấp.
        private String address;               // Địa chỉ nhà cung cấp.
    }
}