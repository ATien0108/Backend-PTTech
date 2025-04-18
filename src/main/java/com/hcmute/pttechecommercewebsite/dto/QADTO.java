package com.hcmute.pttechecommercewebsite.dto;

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
public class QADTO {

    private String id;                          // ID duy nhất của câu hỏi.
    private String productId;                   // ID sản phẩm mà câu hỏi liên quan.
    private String userId;                      // ID người dùng đặt câu hỏi.
    private Date createdAt;                     // Ngày tạo câu hỏi.

    private List<QuestionAnswerDTO> questionAnswers; // Danh sách các câu hỏi và câu trả lời liên tục

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionAnswerDTO {
        private String questionId;  // ID duy nhất của câu hỏi
        private String question;    // Nội dung câu hỏi của người dùng
        private String answer;      // Câu trả lời của quản trị viên
        private String adminId;     // ID của quản trị viên trả lời câu hỏi
        private Date answeredAt;    // Ngày trả lời câu hỏi
        private boolean isAnswered; // Trạng thái câu hỏi đã được trả lời hay chưa
        private List<QuestionAnswerDTO> followUpQuestions; // Các câu hỏi tiếp theo từ người dùng (nếu có)
    }
}