package com.hcmute.pttechecommercewebsite.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;

@Document(collection = "AdImages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdImage {

    @Id
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

    private boolean isActive;                // Trạng thái quảng cáo (active hoặc inactive)

    @NotNull(message = "Loại quảng cáo không được để trống")
    private String adType;                   // Loại quảng cáo ("main" hoặc "secondary")

    @CreatedDate
    private Date createdAt;                  // Thời gian tạo quảng cáo

    @LastModifiedDate
    private Date updatedAt;                  // Thời gian cập nhật quảng cáo

    private boolean isDeleted;               // Trạng thái xóa mềm (true: đã xóa, false: chưa xóa)

    private Date scheduledDate;
}