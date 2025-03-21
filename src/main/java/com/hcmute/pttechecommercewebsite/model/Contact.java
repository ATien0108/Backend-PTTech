package com.hcmute.pttechecommercewebsite.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Document(collection = "Contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    @Id
    private String id;                       // ID duy nhất của thông tin liên hệ.

    @NotNull(message = "Tên công ty không được để trống")
    private String companyName;              // Tên công ty hoặc website.

    @NotNull(message = "Email không được để trống")
    private String email;                    // Địa chỉ email hỗ trợ khách hàng.

    @NotNull(message = "Số điện thoại không được để trống")
    private String phoneNumber;              // Số điện thoại liên hệ hỗ trợ.

    private Address address;                 // Địa chỉ công ty.
    private SocialMedia socialMedia;         // Các kênh mạng xã hội.
    private SupportHours supportHours;       // Giờ làm việc hỗ trợ khách hàng.

    private boolean isActive;                // Trạng thái hiển thị.
    private boolean isDeleted;               // Trạng thái xóa mềm.

    private Date scheduledDate;

    @CreatedDate
    private Date createdAt;                  // Ngày tạo.

    @LastModifiedDate
    private Date updatedAt;                  // Ngày cập nhật.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Address {
        private String street;
        private String city;
        private String district;
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SocialMedia {
        private String facebook;
        private String instagram;
        private String twitter;
        private String zalo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SupportHours {
        private String weekdays;
        private String weekends;
    }
}