package com.hcmute.pttechecommercewebsite.controller;

import com.hcmute.pttechecommercewebsite.dto.QADTO;
import com.hcmute.pttechecommercewebsite.model.QA;
import com.hcmute.pttechecommercewebsite.service.QAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/qas")
public class QAController {

    @Autowired
    private QAService qaService;

    // Lấy tất cả QA và sắp xếp theo thứ tự mới nhất hoặc cũ nhất
    @GetMapping("")
    public ResponseEntity<List<QADTO>> getAllQAs(@RequestParam(defaultValue = "desc") String sortOrder) {
        List<QADTO> qaList = qaService.getAllQAsSorted(sortOrder);
        return new ResponseEntity<>(qaList, HttpStatus.OK);
    }

    // Lấy tất cả QA theo userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QADTO>> getQAsByUserId(@PathVariable String userId) {
        List<QADTO> qaList = qaService.getQAsByUserId(userId);
        return new ResponseEntity<>(qaList, HttpStatus.OK);
    }

    // Lấy tất cả QA theo productId
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<QADTO>> getQAsByProductId(@PathVariable String productId) {
        List<QADTO> qaList = qaService.getQAsByProductId(productId);
        return new ResponseEntity<>(qaList, HttpStatus.OK);
    }

    // Tạo mới QA
    @PostMapping("")
    public ResponseEntity<QADTO> createQA(@RequestBody QA qa) {
        QADTO newQA = qaService.createQA(qa);
        return new ResponseEntity<>(newQA, HttpStatus.CREATED);
    }

    // Chỉnh sửa QA
    @PutMapping("/{id}")
    public ResponseEntity<QADTO> updateQA(@PathVariable String id, @RequestBody QA qa) {
        qa.setId(id);
        QADTO updatedQA = qaService.updateQA(qa);
        return new ResponseEntity<>(updatedQA, HttpStatus.OK);
    }

    // Xóa QA
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQA(@PathVariable String id) {
        qaService.deleteQA(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Trả lời QA
    @PostMapping("/{qaId}/answer")
    public ResponseEntity<QADTO> answerQA(@PathVariable String qaId,
                                          @RequestParam String answer,
                                          @RequestParam String adminId) {
        QADTO updatedQA = qaService.answerQA(qaId, answer, adminId);
        return new ResponseEntity<>(updatedQA, HttpStatus.OK);
    }

    // Chỉnh sửa câu trả lời QA
    @PutMapping("/{qaId}/answer/{questionId}")
    public ResponseEntity<QADTO> updateAnswer(@PathVariable String qaId,
                                              @PathVariable String questionId,
                                              @RequestParam String newAnswer) {
        QADTO updatedQA = qaService.updateAnswer(qaId, questionId, newAnswer);
        return new ResponseEntity<>(updatedQA, HttpStatus.OK);
    }

    // Xóa câu trả lời QA
    @DeleteMapping("/{qaId}/answer/{questionId}")
    public ResponseEntity<QADTO> deleteAnswer(@PathVariable String qaId,
                                              @PathVariable String questionId) {
        QADTO updatedQA = qaService.deleteAnswer(qaId, questionId);
        return new ResponseEntity<>(updatedQA, HttpStatus.NO_CONTENT);
    }

    // Thêm câu hỏi vào QA hiện tại
    @PostMapping("/{qaId}/question")
    public ResponseEntity<QADTO> addQuestionToQA(@PathVariable String qaId,
                                                 @RequestParam String question) {
        QADTO updatedQA = qaService.addQuestionToQA(qaId, question);
        return new ResponseEntity<>(updatedQA, HttpStatus.OK);
    }

    // Cập nhật câu hỏi trong QA
    @PutMapping("/{qaId}/question/{questionId}")
    public ResponseEntity<QADTO> updateQuestionInQA(@PathVariable String qaId,
                                                    @PathVariable String questionId,
                                                    @RequestParam String newQuestion) {
        QADTO updatedQA = qaService.updateQuestionInQA(qaId, questionId, newQuestion);
        return new ResponseEntity<>(updatedQA, HttpStatus.OK);
    }

    // Xóa câu hỏi trong QA
    @DeleteMapping("/{qaId}/question/{questionId}")
    public ResponseEntity<QADTO> deleteQuestionFromQA(@PathVariable String qaId,
                                                      @PathVariable String questionId) {
        QADTO updatedQA = qaService.deleteQuestionFromQA(qaId, questionId);
        return new ResponseEntity<>(updatedQA, HttpStatus.NO_CONTENT);
    }
}
