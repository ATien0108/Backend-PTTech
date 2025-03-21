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

@Document(collection = "Categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    private String id;                       // ID duy nhất của danh mục (được MongoDB tự động tạo)

    @NotNull(message = "Tên danh mục không được để trống")
    @Size(min = 3, max = 100, message = "Tên danh mục phải có độ dài từ 3 đến 100 ký tự")
    private String name;                     // Tên danh mục
    private String description;              // Mô tả danh mục
    private ObjectId parentCategoryId;       // ID danh mục cha (ObjectId thay vì String)
    private String image;                    // Hình ảnh minh họa cho danh mục
    private List<String> tags;               // Các tag liên quan đến danh mục
    private boolean isDeleted;               // Trạng thái xóa mềm (soft delete)
    private boolean isActive;               // Trạng thái hiển thị

    private Date scheduledDate;

    @CreatedDate
    private Date createdAt;                  // Ngày tạo danh mục

    @LastModifiedDate
    private Date updatedAt;                  // Ngày cập nhật danh mục
}