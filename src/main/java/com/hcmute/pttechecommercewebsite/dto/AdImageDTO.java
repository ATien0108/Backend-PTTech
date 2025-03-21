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
public class AdImageDTO {

    @NotNull(message = "ID không được để trống")
    private String id;                       // ID duy nhất của quảng cáo

    @NotNull(message = "Tiêu đề quảng cáo không được để trống")
    @Size(min = 3, max = 100, message = "Tiêu đề quảng cáo phải có độ dài từ 3 đến 100 ký tự")
    private String title;                    // Tiêu đề của quảng cáo

    @NotNull(message = "Hình ảnh không được để trống")
    private String image;                 // Hình ảnh quảng cáo

    private String link;                     // Liên kết đến trang đích khi người dùng nhấn vào quảng cáo

    private String description;              // Mô tả ngắn gọn về quảng cáo

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private Date startDate;                  // Ngày bắt đầu hiển thị quảng cáo

    private Date endDate;                    // Ngày kết thúc quảng cáo (nếu có)

    @JsonProperty("isActive")
    private boolean isActive;                // Trạng thái quảng cáo (active hoặc inactive)

    @NotNull(message = "Loại quảng cáo không được để trống")
    private String adType;                   // Loại quảng cáo (\"main\" hoặc \"secondary\")

    @JsonProperty("isDeleted")
    private boolean isDeleted;               // Trạng thái xóa mềm (true: đã xóa, false: chưa xóa)

    private Date scheduledDate;
}