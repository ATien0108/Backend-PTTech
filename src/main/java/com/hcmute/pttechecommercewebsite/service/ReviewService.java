package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.ReviewDTO;
import com.hcmute.pttechecommercewebsite.model.Product;
import com.hcmute.pttechecommercewebsite.model.Review;
import com.hcmute.pttechecommercewebsite.model.User;
import com.hcmute.pttechecommercewebsite.repository.ProductRepository;
import com.hcmute.pttechecommercewebsite.repository.ReviewRepository;
import com.hcmute.pttechecommercewebsite.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailService emailService;

    // Chuyển đổi từ Review model sang ReviewDTO
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO.ReplyDTO replyDTO = null;
        if (review.getReply() != null) {
            replyDTO = new ReviewDTO.ReplyDTO(review.getReply().getReplyText(), review.getReply().getReplyDate());
        }

        return ReviewDTO.builder()
                .id(review.getId())
                .productId(review.getProductId().toString())
                .productVariantId(review.getProductVariantId() != null ? review.getProductVariantId().toString() : null)
                .userId(review.getUserId().toString())
                .orderId(review.getOrderId().toString())
                .rating(review.getRating())
                .review(review.getReview())
                .reviewTitle(review.getReviewTitle())
                .images(review.getImages())
                .reply(replyDTO)
                .isDeleted(review.isDeleted())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    // Xem tất cả đánh giá chưa bị xóa
    public List<ReviewDTO> getAllReviews() {
        List<Review> reviews = reviewRepository.findByIsDeletedFalse();
        return reviews.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Xem đánh giá theo ID
    public Optional<ReviewDTO> getReviewById(String id) {
        Optional<Review> review = reviewRepository.findByIdAndIsDeletedFalse(id);
        return review.map(this::convertToDTO);
    }

    // Xem tất cả đánh giá của người dùng
    public List<ReviewDTO> getReviewsByUserId(ObjectId userId) {
        List<Review> reviews = reviewRepository.findByUserIdAndIsDeletedFalse(userId);
        return reviews.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Xem tất cả đánh giá của sản phẩm
    public List<ReviewDTO> getReviewsByProductId(ObjectId productId) {
        List<Review> reviews = reviewRepository.findByProductIdAndIsDeletedFalse(productId);
        return reviews.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Xem tất cả đánh giá của đơn hàng
    public List<ReviewDTO> getReviewsByOrderId(ObjectId orderId) {
        List<Review> reviews = reviewRepository.findByOrderIdAndIsDeletedFalse(orderId);
        return reviews.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Thêm đánh giá mới
    public ReviewDTO addReview(Review review) {
        review.setDeleted(false);
        review.setCreatedAt(new Date());
        review.setUpdatedAt(new Date());

        Review savedReview = reviewRepository.save(review);
        String productId = savedReview.getProductId().toString();
        double newRating = savedReview.getRating();
        updateProductRatings(productId, newRating, 0);

        // Gửi email cảm ơn sau khi đánh giá thành công
        emailService.sendThankYouEmail(savedReview);

        return convertToDTO(savedReview);
    }

    // Cập nhật ratings cho sản phẩm sau khi có đánh giá mới
    private void updateProductRatings(String productId, double newRating, double oldRating) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            int totalReviews = product.getRatings().getTotalReviews();
            double averageRating = product.getRatings().getAverage();

            if (oldRating == 0) {
                totalReviews += 1;
                averageRating = (averageRating * (totalReviews - 1) + newRating) / totalReviews;
            } else if (newRating == 0) {
                totalReviews -= 1;
                averageRating = (averageRating * (totalReviews + 1) - oldRating) / totalReviews;
            } else {
                averageRating = (averageRating * totalReviews - oldRating + newRating) / totalReviews;
            }

            product.getRatings().setAverage(averageRating);
            product.getRatings().setTotalReviews(totalReviews);
            productRepository.save(product);
        }
    }

    // Gửi email cảm ơn
    private void sendThankYouEmail(Review review) {
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
                    "Website: <a href='https://www.pttech.com'>www.pttech.com</a><br>" +
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

    // Chỉnh sửa đánh giá
    public Optional<ReviewDTO> updateReview(String id, Review review) {
        Optional<Review> existingReview = reviewRepository.findByIdAndIsDeletedFalse(id);
        if (existingReview.isPresent()) {
            Review oldReview = existingReview.get();

            double oldRating = oldReview.getRating();
            double newRating = review.getRating();

            review.setId(id);
            review.setUpdatedAt(new Date());
            Review updatedReview = reviewRepository.save(review);

            if (oldRating != newRating) {
                updateProductRatings(updatedReview.getProductId().toString(), newRating, oldRating);
            }

            return Optional.of(convertToDTO(updatedReview));
        }
        return Optional.empty();
    }

    // Xóa mềm đánh giá
    public boolean deleteReview(String id) {
        Optional<Review> review = reviewRepository.findByIdAndIsDeletedFalse(id);
        if (review.isPresent()) {
            Review existingReview = review.get();

            double oldRating = existingReview.getRating();
            String productId = existingReview.getProductId().toString();

            existingReview.setDeleted(true);
            existingReview.setUpdatedAt(new Date());
            reviewRepository.save(existingReview);

            updateProductRatings(productId, 0, oldRating);

            return true;
        }
        return false;
    }

    // Trả lời đánh giá
    public Optional<ReviewDTO> replyToReview(String id, String replyText) {
        Optional<Review> review = reviewRepository.findByIdAndIsDeletedFalse(id);
        if (review.isPresent()) {
            Review existingReview = review.get();

            if (existingReview.getReply() == null) {
                existingReview.setReply(new Review.Reply(replyText, new java.util.Date()));
            } else {
                existingReview.getReply().setReplyText(replyText);
                existingReview.getReply().setReplyDate(new java.util.Date());
            }

            reviewRepository.save(existingReview);
            return Optional.of(convertToDTO(existingReview));
        }
        return Optional.empty();
    }
}