package com.hcmute.pttechecommercewebsite.service;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String getVerificationSuccessPage() {
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; color: #333; background-color: #f7f7f7; text-align: center; padding: 50px; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 30px; background-color: white; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }" +
                ".header { font-size: 24px; color: #0056b3; font-weight: bold; }" +
                ".content { margin-top: 20px; font-size: 16px; line-height: 1.5; color: #555; }" +
                ".footer { margin-top: 40px; font-size: 14px; color: #777; }" +
                ".cta { display: block; margin: 20px auto; padding: 12px 20px; background-color: #0056b3; color: white; text-align: center; text-decoration: none; font-weight: bold; border-radius: 4px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>Xác Thực Tài Khoản Thành Công!</div>" +
                "<div class='content'>" +
                "<p>Chúc mừng! Tài khoản của bạn đã được xác thực thành công.</p>" +
                "<p>Giờ đây, bạn có thể bắt đầu sử dụng các dịch vụ của PTTech. Nếu có bất kỳ vấn đề gì, vui lòng liên hệ với chúng tôi qua email <a href='mailto:support@pttech.com'>support@pttech.com</a>.</p>" +
                "<a href='http://localhost:8080' class='cta'>Trở về trang chủ</a>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,<br>PTTech</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public String getVerificationFailurePage() {
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; color: #333; background-color: #f7f7f7; text-align: center; padding: 50px; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 30px; background-color: white; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }" +
                ".header { font-size: 24px; color: #d9534f; font-weight: bold; }" +
                ".content { margin-top: 20px; font-size: 16px; line-height: 1.5; color: #555; }" +
                ".footer { margin-top: 40px; font-size: 14px; color: #777; }" +
                ".cta { display: block; margin: 20px auto; padding: 12px 20px; background-color: #d9534f; color: white; text-align: center; text-decoration: none; font-weight: bold; border-radius: 4px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>Xác Thực Thất Bại!</div>" +
                "<div class='content'>" +
                "<p>Xin lỗi, mã xác thực của bạn đã hết hạn hoặc không hợp lệ.</p>" +
                "<p>Vui lòng thử lại sau hoặc liên hệ với bộ phận hỗ trợ của chúng tôi nếu cần giúp đỡ.</p>" +
                "<a href='http://localhost:8080' class='cta'>Liên hệ với chúng tôi</a>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,<br>PTTech</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}