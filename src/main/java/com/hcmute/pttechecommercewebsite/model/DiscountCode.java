package com.hcmute.pttechecommercewebsite.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Document(collection = "DiscountCodes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountCode {

    @Id
    private String id;                       // ID duy nhất của mã giảm giá (được MongoDB tự động tạo)

    @NotNull(message = "Mã giảm giá không được để trống")
    @Size(min = 3, max = 50, message = "Mã giảm giá phải có độ dài từ 3 đến 50 ký tự")
    private String code;                     // Mã giảm giá

    private String description;              // Mô tả về mã giảm giá

    @NotNull(message = "Loại giảm giá không được để trống")
    private String discountType;             // Loại giảm giá: "percentage" hoặc "fixed"

    @NotNull(message = "Giá trị giảm giá không được để trống")
    private Double discountValue;            // Giá trị giảm giá (phần trăm hoặc số tiền)

    private Double minimumPurchaseAmount;    // Số tiền mua tối thiểu để áp dụng mã giảm giá

    private List<ObjectId> applicableCategories; // Danh sách ID hoặc tên của các danh mục áp dụng

    private List<ObjectId> applicableProducts; // Danh sách ID hoặc tên của các sản phẩm áp dụng

    @NotNull(message = "Loại áp dụng mã giảm giá không được để trống")
    private String appliesTo;                // Loại áp dụng mã giảm giá: "products", "shipping", hoặc "both"

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private Date startDate;                  // Ngày bắt đầu có hiệu lực

    @NotNull(message = "Ngày kết thúc không được để trống")
    private Date endDate;                    // Ngày hết hạn

    private Integer usageLimit;              // Số lần mã giảm giá có thể được sử dụng

    private Integer usageCount;              // Số lần mã giảm giá đã được sử dụng

    private List<ObjectId> usedByUsers;      // Danh sách ID người dùng (userId) đã sử dụng mã giảm giá

    private boolean isActive;                // Trạng thái của mã giảm giá

    private boolean isDeleted;               // Trạng thái xóa mềm

    private Date scheduledDate;

    @CreatedDate
    private Date createdAt;                  // Thời gian tạo mã giảm giá

    @LastModifiedDate
    private Date updatedAt;                  // Thời gian cập nhật mã giảm giá
}