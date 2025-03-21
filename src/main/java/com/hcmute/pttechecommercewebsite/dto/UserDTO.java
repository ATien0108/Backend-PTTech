package com.hcmute.pttechecommercewebsite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private String id;                          // ID duy nhất của người dùng.

    private String username;                    // Tên đăng nhập của người dùng.
    private String email;                       // Địa chỉ email của người dùng.
    private String phoneNumber;                 // Số điện thoại của người dùng.
    private String avatar;                      // URL ảnh đại diện của người dùng.

    private AddressDTO address;                 // Địa chỉ của người dùng.

    @JsonProperty("isVerified")
    private boolean isVerified;                 // Trạng thái xác thực email.
    private String verificationToken;           // Token xác thực gửi qua email.
    private Date verificationExpiry;            // Thời gian hết hạn của token xác thực.

    @JsonProperty("isDeleted")
    private boolean isDeleted;                  // Trạng thái xóa mềm (true: đã xóa, false: chưa xóa).

    @JsonProperty("isBlocked")
    private boolean isBlocked;                  // Trạng thái chặn người dùng (true: bị chặn, false: không bị chặn).
    private String blockReason;                 // Lý do người dùng bị chặn (nếu có).

    private List<RoleDTO> roles;                // Danh sách các vai trò của người dùng.

    @JsonProperty("isSubscribedToEmails")
    private boolean isSubscribedToEmails;       // Trạng thái đăng ký nhận email thông báo.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDTO {
        private String street;                  // Đường phố.
        private String communes;                // Xã/phường.
        private String district;                // Quận/huyện.
        private String city;                    // Thành phố.
        private String country;                 // Quốc gia.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleDTO {
        private String roleName;                 // Tên vai trò (ví dụ: "Admin", "Manager", "Customer").
        private List<String> permissions;        // Danh sách các quyền hạn của vai trò.
    }
}