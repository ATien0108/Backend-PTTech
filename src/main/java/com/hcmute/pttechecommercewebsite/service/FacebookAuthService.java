package com.hcmute.pttechecommercewebsite.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FacebookAuthService {

    private static final String FACEBOOK_GRAPH_API_URL = "https://graph.facebook.com/v10.0/me?fields=id,name,email,picture";

    // Lấy thông tin người dùng từ token Facebook
    public Map<String, Object> getUserInfoFromToken(String accessToken) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // Kiểm tra accessToken để đảm bảo nó không bị null hoặc trống
        if (accessToken == null || accessToken.isEmpty()) {
            throw new Exception("Access token không hợp lệ.");
        }

        String url = FACEBOOK_GRAPH_API_URL + "&access_token=" + accessToken;

        try {
            // Gửi yêu cầu đến API Facebook và lấy dữ liệu
            Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);

            if (userInfo == null) {
                throw new Exception("Không thể lấy thông tin người dùng từ Facebook");
            }

            return userInfo;
        } catch (Exception e) {
            throw new Exception("Lỗi khi gọi API Facebook: " + e.getMessage());
        }
    }
}