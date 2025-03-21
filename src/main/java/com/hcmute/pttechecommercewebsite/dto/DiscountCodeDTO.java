package com.hcmute.pttechecommercewebsite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountCodeDTO {

    @NotNull(message = "ID không được để trống")
    private String id;                       // ID duy nhất của mã giảm giá

    @NotNull(message = "Mã giảm giá không được để trống")
    @Size(min = 3, max = 50, message = "Mã giảm giá phải có độ dài từ 3 đến 50 ký tự")
    private String code;                     // Mã giảm giá

    private String description;              // Mô tả về mã giảm giá

    @NotNull(message = "Loại giảm giá không được để trống")
    private String discountType;             // Loại giảm giá: "percentage" hoặc "fixed"

    @NotNull(message = "Giá trị giảm giá không được để trống")
    private Double discountValue;            // Giá trị giảm giá (phần trăm hoặc số tiền)

    private Double minimumPurchaseAmount;    // Số tiền mua tối thiểu để áp dụng mã giảm giá

    private List<String> applicableCategories; // Danh sách danh mục áp dụng

    private List<String> applicableProducts; // Danh sách sản phẩm áp dụng

    @NotNull(message = "Loại áp dụng mã giảm giá không được để trống")
    private String appliesTo;                // Loại áp dụng mã giảm giá: "products", "shipping", hoặc "both"

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private Date startDate;                  // Ngày bắt đầu có hiệu lực

    @NotNull(message = "Ngày kết thúc không được để trống")
    private Date endDate;                    // Ngày hết hạn

    private Integer usageLimit;              // Số lần mã giảm giá có thể được sử dụng

    private Integer usageCount;              // Số lần mã giảm giá đã được sử dụng

    private List<String> usedByUsers;        // Danh sách ID người dùng (userId) đã sử dụng mã giảm giá

    @JsonProperty("isActive")
    private boolean isActive;                // Trạng thái của mã giảm giá

    @JsonProperty("isDeleted")
    private boolean isDeleted;               // Trạng thái xóa mềm

    private Date scheduledDate;
}