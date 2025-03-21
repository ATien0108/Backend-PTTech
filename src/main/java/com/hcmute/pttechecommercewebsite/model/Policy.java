package com.hcmute.pttechecommercewebsite.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;

@Document(collection = "Policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    private String id; // ID duy nhất của chính sách (do MongoDB tạo)

    @NotNull(message = "Loại chính sách không được để trống")
    private String type; // Loại chính sách

    @NotNull(message = "Tiêu đề chính sách không được để trống")
    @Size(min = 5, max = 100, message = "Tiêu đề chính sách phải có độ dài từ 5 đến 100 ký tự")
    private String title; // Tiêu đề chính sách

    private String description; // Mô tả chính sách

    @NotNull(message = "Nội dung chính sách không được để trống")
    private String content; // Nội dung chính sách

    @NotNull
    private boolean isActive; // Trạng thái hiển thị của chính sách (true: hiển thị, false: không hiển thị)

    @CreatedDate
    private Date createdAt; // Ngày tạo chính sách

    @LastModifiedDate
    private Date updatedAt; // Ngày cập nhật chính sách

    private boolean isDeleted; // Trạng thái xóa mềm (true: đã xóa, false: chưa xóa)

    private Date scheduledDate;
}