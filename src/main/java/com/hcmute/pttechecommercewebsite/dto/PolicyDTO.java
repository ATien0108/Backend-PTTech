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
public class PolicyDTO {

    @NotNull(message = "ID không được để trống")
    private String id; // ID duy nhất của chính sách

    @NotNull(message = "Loại chính sách không được để trống")
    private String type; // Loại chính sách

    @NotNull(message = "Tiêu đề chính sách không được để trống")
    @Size(min = 5, max = 100, message = "Tiêu đề chính sách phải có độ dài từ 5 đến 100 ký tự")
    private String title; // Tiêu đề chính sách

    private String description; // Mô tả chính sách
    private String content; // Nội dung chính sách

    @JsonProperty("isActive")
    private boolean isActive; // Trạng thái hiển thị của chính sách

    @JsonProperty("isDeleted")
    private boolean isDeleted; // Trạng thái xóa mềm

    private Date createdAt;
    private Date updatedAt;
    private Date scheduledDate;
}
