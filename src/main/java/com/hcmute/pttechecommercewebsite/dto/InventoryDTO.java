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
public class InventoryDTO {

    private String id;                       // ID duy nhất của bản ghi nhập kho.
    private List<ProductEntryDTO> products;  // Danh sách các sản phẩm được nhập kho.
    private SupplierDTO supplier;            // Thông tin nhà cung cấp (nếu có).
    private double totalAmount;              // Tổng giá trị của tất cả các sản phẩm nhập kho.
    private int totalQuantity;               // Tổng số lượng sản phẩm nhập kho.
    private String notes;                    // Ghi chú về việc nhập kho.
    private Date receivedDate;               // Ngày nhập kho.

    private boolean isDeleted;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductEntryDTO {
        private String productId;              // ID tham chiếu đến sản phẩm (ObjectId).
        private String productName;            // Tên sản phẩm.
        private List<ProductVariantEntryDTO> productVariants; // Danh sách các biến thể sản phẩm.

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ProductVariantEntryDTO {
            private String productVariantId;    // ID tham chiếu đến biến thể sản phẩm (ObjectId).
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
    public static class SupplierDTO {
        private String name;                  // Tên nhà cung cấp.
        private String contact;               // Thông tin liên hệ của nhà cung cấp.
        private String address;               // Địa chỉ nhà cung cấp.
    }
}