package com.hcmute.pttechecommercewebsite.controller;

import com.hcmute.pttechecommercewebsite.dto.PolicyDTO;
import com.hcmute.pttechecommercewebsite.exception.MessageResponse;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policyService;

    @Autowired
    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    // Xem tất cả chính sách
    @GetMapping
    public ResponseEntity<List<PolicyDTO>> getAllPolicies(
            @RequestParam(value = "sortBy", defaultValue = "title") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {

        List<PolicyDTO> policies = policyService.getAllPolicies(sortBy, sortOrder);
        if (policies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(policies, HttpStatus.OK);
    }

    // Xem tất cả chính sách
    @GetMapping("/no-delete")
    public ResponseEntity<List<PolicyDTO>> getAllPoliciesWithDeletedFalse(
            @RequestParam(value = "sortBy", defaultValue = "title") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {

        List<PolicyDTO> policies = policyService.getAllPoliciesWithDeletedFalse(sortBy, sortOrder);
        if (policies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(policies, HttpStatus.OK);
    }

    // Xem chính sách theo ID
    @GetMapping("/{id}")
    public ResponseEntity<PolicyDTO> getPolicyById(@PathVariable String id) {
        return policyService.getPolicyById(id)
                .map(policyDTO -> new ResponseEntity<>(policyDTO, HttpStatus.OK))
                .orElseThrow(() -> new ResourceNotFoundException("Chính sách với ID " + id + " không tồn tại"));
    }

    // Tìm kiếm chính sách theo tiêu đề
    @GetMapping("/search")
    public ResponseEntity<List<PolicyDTO>> searchPolicies(@RequestParam String keyword) {
        List<PolicyDTO> policies = policyService.searchPoliciesByTitle(keyword);
        if (policies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(policies);
    }

    // Thêm mới chính sách
    @PostMapping
    public ResponseEntity<PolicyDTO> createPolicy(@ModelAttribute PolicyDTO policyDTO) {
        PolicyDTO createdPolicy = policyService.createPolicy(policyDTO);
        return new ResponseEntity<>(createdPolicy, HttpStatus.CREATED);
    }

    // API tạo chính sách với thời gian lên lịch
    @PostMapping("/schedule-create")
    public ResponseEntity<PolicyDTO> scheduleCreatePolicy(@RequestBody PolicyDTO policyDTO) {
        PolicyDTO scheduledPolicy = policyService.scheduleCreatePolicy(policyDTO);
        return new ResponseEntity<>(scheduledPolicy, HttpStatus.CREATED);
    }

    // Chỉnh sửa chính sách
    @PutMapping("/{id}")
    public ResponseEntity<Object> updatePolicy(@PathVariable String id, @ModelAttribute PolicyDTO policyDTO) {
        PolicyDTO updatedPolicy = policyService.updatePolicy(id, policyDTO);
        return new ResponseEntity<>(new MessageResponse("Chỉnh sửa chính sách thành công!", updatedPolicy), HttpStatus.OK);
    }

    // Ẩn chính sách
    @PutMapping("/hide/{id}")
    public ResponseEntity<Object> hidePolicy(@PathVariable String id) {
        PolicyDTO hiddenPolicy = policyService.hidePolicy(id);
        return new ResponseEntity<>(new MessageResponse("Ẩn chính sách thành công!", hiddenPolicy), HttpStatus.OK);
    }

    // Hiện chính sách
    @PutMapping("/show/{id}")
    public ResponseEntity<Object> showPolicy(@PathVariable String id) {
        PolicyDTO shownPolicy = policyService.showPolicy(id);
        return new ResponseEntity<>(new MessageResponse("Hiện chính sách thành công!", shownPolicy), HttpStatus.OK);
    }

    // Xóa chính sách (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePolicy(@PathVariable String id) {
        policyService.deletePolicy(id);
        return new ResponseEntity<>(new MessageResponse("Bạn đã thực hiện xóa thành công chính sách với ID: " + id, id), HttpStatus.OK);
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportPoliciesToExcel(
            @RequestParam(value = "sortBy", defaultValue = "title") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {

        try {
            ByteArrayOutputStream outputStream = policyService.exportPoliciesToExcel(sortBy, sortOrder);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=policies.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Lỗi khi xuất file Excel: " + e.getMessage()).getBytes());
        }
    }
}