package com.hcmute.pttechecommercewebsite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactDTO {

    private String id;                       // ID duy nhất của thông tin liên hệ
    private String companyName;              // Tên công ty hoặc website
    private String email;                    // Địa chỉ email hỗ trợ khách hàng
    private String phoneNumber;              // Số điện thoại liên hệ
    private AddressDTO address;              // Địa chỉ công ty
    private SocialMediaDTO socialMedia;      // Các kênh mạng xã hội
    private SupportHoursDTO supportHours;    // Giờ làm việc hỗ trợ khách hàng
    @JsonProperty("isActive")
    private boolean isActive;                // Trạng thái hiển thị
    @JsonProperty("isDeleted")
    private boolean isDeleted;               // Trạng thái xóa mềm

    private Date scheduledDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDTO {
        private String street;
        private String city;
        private String district;
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SocialMediaDTO {
        private String facebook;
        private String instagram;
        private String twitter;
        private String zalo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SupportHoursDTO {
        private String weekdays;
        private String weekends;
    }
}
