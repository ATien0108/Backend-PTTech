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

@Document(collection = "Brands")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

    @Id
    private String id;                       // ID duy nhất của thương hiệu (được MongoDB tự động tạo)

    @NotNull(message = "Tên thương hiệu không được để trống")
    @Size(min = 3, max = 100, message = "Tên thương hiệu phải có độ dài từ 3 đến 100 ký tự")
    private String name;                     // Tên thương hiệu

    private String description;              // Mô tả chi tiết về thương hiệu
    private String logo;                     // Logo thương hiệu
    private String country;                  // Quốc gia của thương hiệu
    private String website;                  // Website chính thức của thương hiệu
    private boolean isDeleted;               // Trạng thái xóa mềm (soft delete)
    private boolean isActive;               // Trạng thái hiển thị

    private Date scheduledDate;

    @CreatedDate
    private Date createdAt;                  // Ngày tạo thương hiệu

    @LastModifiedDate
    private Date updatedAt;                  // Ngày cập nhật thương hiệu
}