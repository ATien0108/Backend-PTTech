package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.model.Review;
import com.hcmute.pttechecommercewebsite.model.User;
import com.hcmute.pttechecommercewebsite.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    // Gửi email xác thực
    public void sendVerificationEmail(User user) {
        String subject = "Xác Thực Tài Khoản - PTTech";
        String verificationUrl = "http://localhost:8081/api/users/verify?token=" + user.getVerificationToken();

        String emailContent = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9; }" +
                ".header { text-align: center; font-size: 24px; font-weight: bold; color: #0056b3; }" +
                ".content { margin-top: 20px; font-size: 16px; line-height: 1.5; }" +
                ".cta { display: block; margin: 20px auto; padding: 12px 20px; background-color: #0056b3; color: white; text-align: center; text-decoration: none; font-weight: bold; border-radius: 4px; }" +
                ".footer { text-align: center; margin-top: 40px; font-size: 14px; color: #777; }" +
                ".footer a { color: #0056b3; text-decoration: none; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>Chào " + user.getUsername() + ",</div>" +
                "<div class='content'>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản tại PTTech. Để bảo vệ tài khoản của bạn và xác nhận rằng bạn đã đăng ký đúng email này, vui lòng nhấp vào liên kết dưới đây để hoàn tất quá trình xác thực:</p>" +
                "<a href='" + verificationUrl + "' class='cta'>Xác thực tài khoản của bạn</a>" +
                "<p>Liên kết xác thực sẽ hết hạn sau 24 giờ. Nếu bạn không yêu cầu đăng ký, bạn có thể bỏ qua email này.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,</p>" +
                "<p><b>PTTech</b><br>" +
                "Địa chỉ: 01 Đường Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh<br>" +
                "Website: <a href='http://localhost:8080'>www.pttech.com</a><br>" +
                "Email hỗ trợ: <a href='mailto:support@pttech.com'>support@pttech.com</a></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Gửi email reset mật khẩu
    public void sendPasswordResetEmail(User user) {
        String subject = "Yêu cầu thay đổi mật khẩu - PTTech";
        String resetUrl = "http://localhost:8081/api/users/reset-password?token=" + user.getVerificationToken();

        String emailContent = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9; }" +
                ".header { text-align: center; font-size: 24px; font-weight: bold; color: #0056b3; }" +
                ".content { margin-top: 20px; font-size: 16px; line-height: 1.5; }" +
                ".cta { display: block; margin: 20px auto; padding: 12px 20px; background-color: #0056b3; color: white; text-align: center; text-decoration: none; font-weight: bold; border-radius: 4px; }" +
                ".footer { text-align: center; margin-top: 40px; font-size: 14px; color: #777; }" +
                ".footer a { color: #0056b3; text-decoration: none; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>Chào " + user.getUsername() + ",</div>" +
                "<div class='content'>" +
                "<p>Để thay đổi mật khẩu của bạn, vui lòng nhấp vào liên kết dưới đây:</p>" +
                "<a href='" + resetUrl + "' class='cta'>Thay đổi mật khẩu của bạn</a>" +
                "<p>Liên kết này sẽ hết hạn sau 1 giờ. Nếu bạn không yêu cầu thay đổi mật khẩu, vui lòng bỏ qua email này.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,</p>" +
                "<p><b>PTTech</b><br>" +
                "Địa chỉ: 01 Đường Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh<br>" +
                "Website: <a href='http://localhost:8080'>www.pttech.com</a><br>" +
                "Email hỗ trợ: <a href='mailto:support@pttech.com'>support@pttech.com</a></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Gửi email thông báo
    public void sendNotificationEmail(String subject, String content, String userEmail) {
        String emailContent = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9; }" +
                ".header { text-align: center; font-size: 24px; font-weight: bold; color: #0056b3; }" +
                ".content { margin-top: 20px; font-size: 16px; line-height: 1.5; }" +
                ".footer { text-align: center; margin-top: 40px; font-size: 14px; color: #777; }" +
                ".footer a { color: #0056b3; text-decoration: none; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>Thông Báo Quan Trọng từ PTTech</div>" +
                "<div class='content'>" +
                "<p>" + content + "</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,</p>" +
                "<p><b>PTTech</b><br>" +
                "Địa chỉ: 01 Đường Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh<br>" +
                "Website: <a href='http://localhost:8080'>www.pttech.com</a><br>" +
                "Email hỗ trợ: <a href='mailto:support@pttech.com'>support@pttech.com</a></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(userEmail);
            helper.setSubject(subject);
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Gửi email cảm ơn
    public void sendThankYouEmail(Review review) {
        Optional<User> userOpt = userRepository.findById(review.getUserId().toString());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String userEmail = user.getEmail();
            String userName = user.getUsername();

            String subject = "Cảm ơn bạn đã đánh giá sản phẩm!";
            String emailContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; color: #333; }" +
                    ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9; }" +
                    ".header { text-align: center; font-size: 24px; font-weight: bold; color: #0056b3; }" +
                    ".content { margin-top: 20px; font-size: 16px; line-height: 1.5; }" +
                    ".footer { text-align: center; margin-top: 40px; font-size: 14px; color: #777; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<div class='header'>Cảm ơn bạn, " + userName + "!</div>" +
                    "<div class='content'>" +
                    "<p>Chúng tôi rất vui mừng khi nhận được đánh giá của bạn! Cảm ơn bạn đã dành thời gian chia sẻ cảm nhận về sản phẩm của chúng tôi.</p>" +
                    "<p>Đánh giá của bạn không chỉ giúp chúng tôi cải thiện chất lượng sản phẩm mà còn giúp cho những khách hàng khác có thể tìm thấy những sản phẩm phù hợp nhất với nhu cầu của mình.</p>" +
                    "<p><b>Thông tin đánh giá của bạn:</b></p>" +
                    "<ul>" +
                    "<li><b>Tiêu đề đánh giá:</b> " + review.getReviewTitle() + "</li>" +
                    "<li><b>Đánh giá:</b> " + review.getReview() + "</li>" +
                    "<li><b>Đánh giá sao:</b> " + review.getRating() + " sao</li>" +
                    "</ul>" +
                    "<p>Chúng tôi cam kết tiếp tục cải tiến sản phẩm và dịch vụ để mang đến cho bạn những trải nghiệm tốt nhất.</p>" +
                    "<p>Trân trọng,</p>" +
                    "<p><b>PTTech</b><br>" +
                    "Website: <a href='http://localhost:8080'>www.pttech.com</a><br>" +
                    "Email hỗ trợ: <a href='mailto:support@pttech.com'>support@pttech.com</a><br>" +
                    "Hotline: 123-456-789</p>" +
                    "</div>" +
                    "<div class='footer'>" +
                    "<p>Chúng tôi rất mong muốn nhận thêm phản hồi từ bạn để không ngừng cải thiện dịch vụ!</p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setTo(userEmail);
                helper.setSubject(subject);
                helper.setText(emailContent, true);
                mailSender.send(mimeMessage);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendLoginSuccessNotificationEmail(User user, String loginMethod) {
        String subject = "Thông Báo Đăng Nhập Thành Công - PTTech";

        // Nội dung email
        String emailContent = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9; }" +
                ".header { text-align: center; font-size: 24px; font-weight: bold; color: #0056b3; }" +
                ".content { margin-top: 20px; font-size: 16px; line-height: 1.5; }" +
                ".cta { display: block; margin: 20px auto; padding: 12px 20px; background-color: #0056b3; color: #ffffff; text-align: center; text-decoration: none; font-weight: bold; border-radius: 4px; }" +
                ".footer { text-align: center; margin-top: 40px; font-size: 14px; color: #777; }" +
                ".footer a { color: #0056b3; text-decoration: none; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>Xin Chào " + user.getUsername() + ",</div>" +
                "<div class='content'>" +
                "<p>Chúng tôi vui mừng thông báo rằng bạn đã đăng nhập thành công vào tài khoản PTTech của mình bằng " + loginMethod + ".</p>" +
                "<p>Đây là thông tin đăng nhập của bạn:</p>" +
                "<ul>" +
                "<li><b>Phương thức đăng nhập:</b> " + loginMethod + "</li>" +
                "<li><b>Tên người dùng:</b> " + user.getUsername() + "</li>" +
                "<li><b>Địa chỉ email:</b> " + user.getEmail() + "</li>" +
                "</ul>" +
                "<p>Chúng tôi luôn cam kết bảo mật thông tin của bạn và cung cấp những trải nghiệm tuyệt vời nhất khi bạn sử dụng dịch vụ của PTTech.</p>" +
                "<p>Trong trường hợp bạn không thực hiện đăng nhập này, vui lòng liên hệ với chúng tôi ngay lập tức để bảo vệ tài khoản của bạn.</p>" +
                "<p>Để tiếp tục mua sắm hoặc truy cập vào tài khoản của bạn, bạn có thể nhấp vào nút dưới đây:</p>" +
                "<a href='http://localhost:8080' class='cta'>Truy Cập Tài Khoản PTTech</a>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,</p>" +
                "<p><b>PTTech</b><br>" +
                "Địa chỉ: 01 Đường Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh<br>" +
                "Website: <a href='http://localhost:8080'>www.pttech.com</a><br>" +
                "Email hỗ trợ: <a href='mailto:support@pttech.com'>support@pttech.com</a><br>" +
                "Hotline: 123-456-789</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        // Gửi email
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Gửi email cảm ơn khi đăng ký nhận thông báo
    public void sendThankYouForSubscriptionEmail(User user) {
        String subject = "Cảm ơn bạn đã đăng ký nhận thông báo - PTTech";

        String emailContent = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9; }" +
                ".header { text-align: center; font-size: 24px; font-weight: bold; color: #0056b3; }" +
                ".content { margin-top: 20px; font-size: 16px; line-height: 1.5; }" +
                ".cta { display: block; margin: 20px auto; padding: 12px 20px; background-color: #0056b3; color: white; text-align: center; text-decoration: none; font-weight: bold; border-radius: 4px; }" +
                ".footer { text-align: center; margin-top: 40px; font-size: 14px; color: #777; }" +
                ".footer a { color: #0056b3; text-decoration: none; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>Cảm ơn bạn, " + user.getUsername() + "!</div>" +
                "<div class='content'>" +
                "<p>Chúng tôi rất vui mừng khi bạn đã đăng ký nhận thông báo từ PTTech! Giờ đây, bạn sẽ nhận được thông tin và cập nhật mới nhất từ chúng tôi qua email.</p>" +
                "<p>Chúng tôi cam kết sẽ không làm bạn thất vọng và sẽ cung cấp những thông tin hữu ích, cập nhật về các sản phẩm và dịch vụ mới nhất của PTTech.</p>" +
                "<p>Nếu bạn có bất kỳ câu hỏi hoặc phản hồi nào, đừng ngần ngại liên hệ với chúng tôi qua email hoặc qua website.</p>" +
                "<p>Cảm ơn bạn một lần nữa vì sự tin tưởng của bạn!</p>" +
                "<a href='http://localhost:8080' class='cta'>Khám phá các sản phẩm của chúng tôi</a>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,</p>" +
                "<p><b>PTTech</b><br>" +
                "Địa chỉ: 01 Đường Võ Văn Ngân, Phường Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh<br>" +
                "Website: <a href='http://localhost:8080'>www.pttech.com</a><br>" +
                "Email hỗ trợ: <a href='mailto:support@pttech.com'>support@pttech.com</a><br>" +
                "Hotline: 123-456-789</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}