package com.hcmute.pttechecommercewebsite.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "QAs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QA {

    @Id
    private String id;              // ID duy nhất của QA
    private ObjectId productId;       // ID của sản phẩm mà câu hỏi liên quan
    private ObjectId userId;          // ID người dùng đặt câu hỏi

    @CreatedDate
    private Date createdAt;         // Ngày tạo QA

    private List<QuestionAnswer> questionAnswers; // Danh sách các câu hỏi và câu trả lời liên tục

    // Cấu trúc cho mỗi câu hỏi và câu trả lời liên tục
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionAnswer {
        private ObjectId questionId;  // ID duy nhất của câu hỏi
        private String question;    // Nội dung câu hỏi của người dùng
        private String answer;      // Câu trả lời của quản trị viên
        private ObjectId adminId;     // ID của quản trị viên trả lời câu hỏi
        private Date answeredAt;    // Ngày trả lời câu hỏi
        private boolean isAnswered; // Trạng thái câu hỏi đã được trả lời hay chưa
        private List<QuestionAnswer> followUpQuestions; // Các câu hỏi tiếp theo từ người dùng (nếu có)
    }
}
