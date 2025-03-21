package com.hcmute.pttechecommercewebsite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    @NotNull(message = "ID không được để trống")
    private String id;                       // ID duy nhất của danh mục

    @NotNull(message = "Tên danh mục không được để trống")
    @Size(min = 3, max = 100, message = "Tên danh mục phải có độ dài từ 3 đến 100 ký tự")
    private String name;                     // Tên danh mục
    private String description;              // Mô tả danh mục
    private String parentCategoryId;         // ID danh mục cha
    private String image;                    // Hình ảnh minh họa cho danh mục
    private List<String> tags;               // Các tag liên quan đến danh mục

    @JsonProperty("isDeleted")
    private boolean isDeleted;               // Trạng thái xóa mềm

    @JsonProperty("isActive")
    private boolean isActive;               // Trạng thái hiển thị

    private Date scheduledDate;
}