package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.QADTO;
import com.hcmute.pttechecommercewebsite.model.QA;
import com.hcmute.pttechecommercewebsite.repository.QARepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QAService {

    @Autowired
    private QARepository qaRepository;

    // Lấy tất cả QA và sắp xếp theo ngày tạo (mới nhất hoặc cũ nhất)
    public List<QADTO> getAllQAsSorted(String sortOrder) {
        List<QA> qas;

        if ("asc".equalsIgnoreCase(sortOrder)) {
            // Sắp xếp từ cũ nhất
            qas = qaRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Order.asc("createdAt")))).getContent();
        } else {
            // Mặc định là sắp xếp mới nhất
            qas = qaRepository.findAll(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Order.desc("createdAt")))).getContent();
        }

        return qas.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Lấy tất cả QA theo userId
    public List<QADTO> getQAsByUserId(String userId) {
        List<QA> qas = qaRepository.findByUserId(new ObjectId(userId));
        return qas.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Lấy tất cả QA theo productId
    public List<QADTO> getQAsByProductId(String productId) {
        List<QA> qas = qaRepository.findByProductId(new ObjectId(productId));
        return qas.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Tạo mới QA
    public QADTO createQA(QA qa) {
        qa.setCreatedAt(new Date());

        // Cập nhật danh sách câu hỏi
        List<QA.QuestionAnswer> questionAnswers = qa.getQuestionAnswers().stream().map(questionAnswer -> {
            if (questionAnswer.getQuestionId() == null) {
                questionAnswer.setQuestionId(new ObjectId());
            }
            questionAnswer.setAnswer("");
            questionAnswer.setAdminId(null);
            questionAnswer.setAnsweredAt(null);
            questionAnswer.setAnswered(false);
            questionAnswer.setFollowUpQuestions(List.of());
            return questionAnswer;
        }).collect(Collectors.toList());

        qa.setQuestionAnswers(questionAnswers);

        // Lưu QA vào cơ sở dữ liệu
        qa = qaRepository.save(qa);

        // Chuyển đổi và trả về QADTO
        return convertToDTO(qa);
    }

    // Chỉnh sửa QA
    public QADTO updateQA(QA qa) {
        qa = qaRepository.save(qa);
        return convertToDTO(qa);
    }

    // Xóa QA
    public void deleteQA(String id) {
        qaRepository.deleteById(id);
    }

    // Trả lời QA
    public QADTO answerQA(String qaId, String answer, String adminId) {
        // Lấy bản ghi QA từ ID
        QA qa = qaRepository.findById(qaId).orElseThrow(() -> new RuntimeException("QA not found"));

        // Chuyển adminId thành ObjectId
        ObjectId adminObjectId = new ObjectId(adminId);

        // Tìm câu hỏi chưa có câu trả lời
        QA.QuestionAnswer questionAnswer = qa.getQuestionAnswers().stream()
                .filter(q -> q.getQuestionId() != null && !q.isAnswered())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No unanswered questions found"));

        // Cập nhật câu trả lời
        questionAnswer.setAnswer(answer);
        questionAnswer.setAdminId(adminObjectId);
        questionAnswer.setAnsweredAt(new Date());
        questionAnswer.setAnswered(true);

        qa = qaRepository.save(qa);

        return convertToDTO(qa);
    }

    // Chỉnh sửa câu trả lời QA
    public QADTO updateAnswer(String qaId, String questionId, String newAnswer) {
        QA qa = qaRepository.findById(qaId).orElseThrow(() -> new RuntimeException("QA not found"));
        for (QA.QuestionAnswer questionAnswer : qa.getQuestionAnswers()) {
            if (questionAnswer.getQuestionId().toString().equals(questionId)) {
                questionAnswer.setAnswer(newAnswer);
                qa = qaRepository.save(qa);
                return convertToDTO(qa);
            }
        }
        throw new RuntimeException("QuestionAnswer not found");
    }

    // Xóa câu trả lời QA
    public QADTO deleteAnswer(String qaId, String questionId) {
        // Lấy bản ghi QA từ ID
        QA qa = qaRepository.findById(qaId).orElseThrow(() -> new RuntimeException("QA not found"));

        // Tìm câu trả lời cần xóa
        QA.QuestionAnswer questionAnswer = qa.getQuestionAnswers().stream()
                .filter(q -> q.getQuestionId() != null && q.getQuestionId().toString().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Answer not found for the given questionId"));

        // Xóa câu trả lời
        questionAnswer.setAnswer(null);
        questionAnswer.setAdminId(null);
        questionAnswer.setAnsweredAt(null);
        questionAnswer.setAnswered(false);

        qa = qaRepository.save(qa);

        return convertToDTO(qa);
    }

    public QADTO addFollowUpQuestion(String qaId, String parentQuestionId, String newQuestion) {
        QA qa = qaRepository.findById(qaId)
                .orElseThrow(() -> new RuntimeException("QA not found"));

        // Tìm câu hỏi cha
        QA.QuestionAnswer parentQuestion = qa.getQuestionAnswers().stream()
                .filter(q -> q.getQuestionId().toString().equals(parentQuestionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Parent question not found"));

        // Tạo câu hỏi tiếp theo
        QA.QuestionAnswer followUp = QA.QuestionAnswer.builder()
                .questionId(new ObjectId())
                .question(newQuestion)
                .answer("")
                .adminId(null)
                .answeredAt(null)
                .isAnswered(false)
                .followUpQuestions(List.of())
                .build();

        if (parentQuestion.getFollowUpQuestions() == null) {
            parentQuestion.setFollowUpQuestions(new java.util.ArrayList<>());
        }

        parentQuestion.getFollowUpQuestions().add(followUp);

        qa = qaRepository.save(qa);

        return convertToDTO(qa);
    }

    public QADTO answerFollowUpQuestion(String qaId, String followUpQuestionId, String answer, String adminId) {
        QA qa = qaRepository.findById(qaId)
                .orElseThrow(() -> new RuntimeException("QA not found"));

        ObjectId adminObjectId = new ObjectId(adminId);

        boolean answered = false;

        // Lặp qua từng câu hỏi chính
        for (QA.QuestionAnswer questionAnswer : qa.getQuestionAnswers()) {
            if (questionAnswer.getFollowUpQuestions() != null) {
                for (QA.QuestionAnswer followUp : questionAnswer.getFollowUpQuestions()) {
                    if (followUp.getQuestionId().toString().equals(followUpQuestionId)) {
                        followUp.setAnswer(answer);
                        followUp.setAdminId(adminObjectId);
                        followUp.setAnsweredAt(new Date());
                        followUp.setAnswered(true);
                        answered = true;
                        break;
                    }
                }
            }
            if (answered) break;
        }

        if (!answered) {
            throw new RuntimeException("Follow-up question not found");
        }

        qa = qaRepository.save(qa);

        return convertToDTO(qa);
    }

    public QADTO deleteFollowUpQuestion(String qaId, String followUpQuestionId) {
        QA qa = qaRepository.findById(qaId)
                .orElseThrow(() -> new RuntimeException("QA not found"));

        boolean removed = false;

        for (QA.QuestionAnswer questionAnswer : qa.getQuestionAnswers()) {
            if (questionAnswer.getFollowUpQuestions() != null) {
                List<QA.QuestionAnswer> followUps = questionAnswer.getFollowUpQuestions();
                removed = followUps.removeIf(fq -> fq.getQuestionId().toString().equals(followUpQuestionId));
                if (removed) break;
            }
        }

        if (!removed) {
            throw new RuntimeException("Follow-up question not found");
        }

        qa = qaRepository.save(qa);

        return convertToDTO(qa);
    }

    // Chuyển đổi từ model QA sang DTO
    private QADTO convertToDTO(QA qa) {
        return QADTO.builder()
                .id(qa.getId())
                .productId(qa.getProductId() != null ? qa.getProductId().toString() : null)
                .userId(qa.getUserId() != null ? qa.getUserId().toString() : null)
                .createdAt(qa.getCreatedAt())
                .questionAnswers(qa.getQuestionAnswers().stream()
                        .map(this::convertToQuestionAnswerDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    // Chuyển đổi từ model QuestionAnswer sang DTO
    private QADTO.QuestionAnswerDTO convertToQuestionAnswerDTO(QA.QuestionAnswer questionAnswer) {
        List<QADTO.QuestionAnswerDTO> followUps = null;
        if (questionAnswer.getFollowUpQuestions() != null) {
            followUps = questionAnswer.getFollowUpQuestions().stream()
                    .map(this::convertToQuestionAnswerDTO)
                    .collect(Collectors.toList());
        }

        return QADTO.QuestionAnswerDTO.builder()
                .questionId(questionAnswer.getQuestionId() != null ? questionAnswer.getQuestionId().toString() : null)
                .question(questionAnswer.getQuestion())
                .answer(questionAnswer.getAnswer())
                .adminId(questionAnswer.getAdminId() != null ? questionAnswer.getAdminId().toString() : null)
                .answeredAt(questionAnswer.getAnsweredAt())
                .isAnswered(questionAnswer.isAnswered())
                .followUpQuestions(followUps)
                .build();
    }
}
