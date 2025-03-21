package com.hcmute.pttechecommercewebsite.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;                          // ID duy nhất của người dùng.

    private String username;                    // Tên đăng nhập của người dùng.
    private String email;                       // Địa chỉ email của người dùng.
    private String password;                    // Mật khẩu của người dùng (mã hóa).
    private String phoneNumber;                 // Số điện thoại của người dùng.
    private String avatar;                      // URL ảnh đại diện của người dùng.

    private Address address;                    // Địa chỉ của người dùng.
    private boolean isVerified;                 // Trạng thái xác thực email.
    private String verificationToken;           // Token xác thực gửi qua email.
    private Date verificationExpiry;            // Thời gian hết hạn của token xác thực.

    private boolean isDeleted;                  // Trạng thái xóa mềm (true: đã xóa, false: chưa xóa).
    private boolean isBlocked;                  // Trạng thái chặn người dùng (true: bị chặn, false: không bị chặn).
    private String blockReason;                 // Lý do người dùng bị chặn (nếu có).

    private List<Role> roles;                   // Danh sách các vai trò của người dùng.
    private boolean isSubscribedToEmails;       // Trạng thái đăng ký nhận email thông báo.

    @CreatedDate
    private Date createdAt;                     // Thời gian tạo tài khoản người dùng.

    @LastModifiedDate
    private Date updatedAt;                     // Thời gian cập nhật thông tin người dùng.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Address {
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
    public static class Role {
        private String roleName;                 // Tên vai trò (ví dụ: "Admin", "Manager", "Customer").
        private List<String> permissions;        // Danh sách các quyền hạn của vai trò.
    }
}