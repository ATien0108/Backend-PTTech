package com.hcmute.pttechecommercewebsite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandDTO {

    @NotNull(message = "ID không được để trống")
    private String id;                       // ID duy nhất của thương hiệu

    @NotNull(message = "Tên thương hiệu không được để trống")
    @Size(min = 3, max = 100, message = "Tên thương hiệu phải có độ dài từ 3 đến 100 ký tự")
    private String name;                     // Tên thương hiệu
    private String description;              // Mô tả chi tiết về thương hiệu
    private String logo;                     // Logo thương hiệu
    private String country;                  // Quốc gia của thương hiệu
    private String website;                  // Website chính thức của thương hiệu

    @JsonProperty("isDeleted")
    private boolean isDeleted;               // Trạng thái xóa mềm

    @JsonProperty("isActive")
    private boolean isActive;               // Trạng thái hiển thị

    private Date scheduledDate;
}