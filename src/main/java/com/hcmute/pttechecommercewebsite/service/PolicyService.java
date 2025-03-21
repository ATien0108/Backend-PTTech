package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.PolicyDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.model.Policy;
import com.hcmute.pttechecommercewebsite.repository.PolicyRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
public class PolicyService {

    private final PolicyRepository policyRepository;

    @Autowired
    public PolicyService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    // Chuyển Entity thành DTO
    private PolicyDTO convertToDTO(Policy policy) {
        return PolicyDTO.builder()
                .id(policy.getId())
                .type(policy.getType())
                .title(policy.getTitle())
                .description(policy.getDescription())
                .content(policy.getContent())
                .isActive(policy.isActive())
                .isDeleted(policy.isDeleted())
                .build();
    }

    // Lấy tất cả chính sách không bị xóa
    public List<PolicyDTO> getAllPolicies(String sortBy, String sortOrder) {
        // Chọn hướng sắp xếp: ASC hoặc DESC
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        // Lấy danh sách các chính sách từ cơ sở dữ liệu với sắp xếp
        List<Policy> policies = policyRepository.findByIsDeletedFalseAndIsActiveTrue(sort);
        return policies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy tất cả chính sách không bị xóa
    public List<PolicyDTO> getAllPoliciesWithDeletedFalse(String sortBy, String sortOrder) {
        // Chọn hướng sắp xếp: ASC hoặc DESC
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        // Lấy danh sách các chính sách từ cơ sở dữ liệu với sắp xếp
        List<Policy> policies = policyRepository.findByIsDeletedFalse(sort);
        return policies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy chính sách theo ID
    public Optional<PolicyDTO> getPolicyById(String id) {
        Optional<Policy> policy = policyRepository.findById(id);
        return policy.map(this::convertToDTO);
    }

    // Tìm kiếm chính sách theo tiêu đề
    public List<PolicyDTO> searchPoliciesByTitle(String keyword) {
        List<Policy> policies = policyRepository.findByTitleContaining(keyword);
        return policies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Tạo mới chính sách
    public PolicyDTO createPolicy(PolicyDTO policyDTO) {
        // Chuyển đổi PolicyDTO thành Policy entity
        Policy policy = Policy.builder()
                .type(policyDTO.getType())
                .title(policyDTO.getTitle())
                .description(policyDTO.getDescription())
                .content(policyDTO.getContent())
                .isActive(true)
                .isDeleted(false)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        // Lưu chính sách vào DB
        Policy savedPolicy = policyRepository.save(policy);
        return convertToDTO(savedPolicy);
    }

    public PolicyDTO scheduleCreatePolicy(PolicyDTO policyDTO) {
        if (policyDTO.getScheduledDate() != null && policyDTO.getScheduledDate().before(new Date())) {
            throw new IllegalArgumentException("Thời gian lên lịch phải là thời gian trong tương lai.");
        }

        // Tạo chính sách mới từ PolicyDTO
        Policy policy = Policy.builder()
                .type(policyDTO.getType())
                .title(policyDTO.getTitle())
                .description(policyDTO.getDescription())
                .content(policyDTO.getContent())
                .isActive(false)
                .isDeleted(false)
                .scheduledDate(policyDTO.getScheduledDate())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        Policy savedPolicy = policyRepository.save(policy);
        return convertToDTO(savedPolicy);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndActivateScheduledPolicies() {
        Date now = new Date();

        List<Policy> scheduledPolicies = policyRepository.findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(now);

        for (Policy policy : scheduledPolicies) {
            policy.setActive(true);
            policy.setScheduledDate(null);
            policy.setUpdatedAt(now);
            policyRepository.save(policy);
        }
    }

    // Chỉnh sửa chính sách
    public PolicyDTO updatePolicy(String id, PolicyDTO policyDTO) {
        Optional<Policy> existingPolicy = policyRepository.findById(id);

        if (existingPolicy.isPresent()) {
            Policy policy = existingPolicy.get();
            policy.setType(policyDTO.getType());
            policy.setTitle(policyDTO.getTitle());
            policy.setDescription(policyDTO.getDescription());
            policy.setContent(policyDTO.getContent());
            policy.setUpdatedAt(new Date());

            // Lưu lại chính sách đã được chỉnh sửa
            Policy updatedPolicy = policyRepository.save(policy);
            return convertToDTO(updatedPolicy);
        } else {
            throw new ResourceNotFoundException("Chính sách với ID " + id + " không tồn tại");
        }
    }

    // Ẩn chính sách
    public PolicyDTO hidePolicy(String id) {
        Optional<Policy> optionalPolicy = policyRepository.findById(id);
        if (!optionalPolicy.isPresent()) {
            throw new ResourceNotFoundException("Chính sách với ID " + id + " không tồn tại");
        }

        Policy policy = optionalPolicy.get();
        policy.setActive(false);
        policy.setUpdatedAt(new Date());
        policy = policyRepository.save(policy);
        return convertToDTO(policy);
    }

    // Hiện chính sách
    public PolicyDTO showPolicy(String id) {
        Optional<Policy> optionalPolicy = policyRepository.findById(id);
        if (!optionalPolicy.isPresent()) {
            throw new ResourceNotFoundException("Chính sách với ID " + id + " không tồn tại");
        }

        Policy policy = optionalPolicy.get();
        policy.setActive(true);
        policy.setUpdatedAt(new Date());
        policy = policyRepository.save(policy);
        return convertToDTO(policy);
    }

    // Xóa chính sách (soft delete)
    public void deletePolicy(String id) {
        Optional<Policy> existingPolicy = policyRepository.findById(id);

        if (existingPolicy.isPresent()) {
            Policy policy = existingPolicy.get();
            policy.setDeleted(true);
            policy.setUpdatedAt(new Date());

            // Lưu chính sách đã bị xóa
            policyRepository.save(policy);
        } else {
            throw new ResourceNotFoundException("Chính sách với ID " + id + " không tồn tại hoặc đã bị xóa");
        }
    }

    // Phương thức xuất tất cả chính sách ra file Excel
    public ByteArrayOutputStream exportPoliciesToExcel(String sortBy, String sortOrder) throws IOException {
        // Lấy danh sách các chính sách từ service với sắp xếp
        List<PolicyDTO> policies = getAllPoliciesWithDeletedFalse(sortBy, sortOrder);

        // Tạo workbook Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Policies");

        // Định dạng chung cho workbook
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Tạo dòng tiêu đề
        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Loại chính sách", "Tiêu đề", "Mô tả", "Nội dung", "Trạng thái hoạt động", "Trạng thái xóa"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Định dạng cho dữ liệu
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Thêm dữ liệu chính sách vào file Excel
        int rowNum = 1;
        for (PolicyDTO policy : policies) {
            Row row = sheet.createRow(rowNum++);

            // Cột ID
            row.createCell(0).setCellValue(policy.getId());
            row.getCell(0).setCellStyle(dataStyle);

            // Cột Loại chính sách
            row.createCell(1).setCellValue(policy.getType());
            row.getCell(1).setCellStyle(dataStyle);

            // Cột Tiêu đề
            row.createCell(2).setCellValue(policy.getTitle());
            row.getCell(2).setCellStyle(dataStyle);

            // Cột Mô tả
            row.createCell(3).setCellValue(policy.getDescription() != null ? policy.getDescription() : "Không có");
            row.getCell(3).setCellStyle(dataStyle);

            // Cột Nội dung
            row.createCell(4).setCellValue(policy.getContent() != null ? policy.getContent() : "Không có");
            row.getCell(4).setCellStyle(dataStyle);

            // Cột Trạng thái hoạt động
            row.createCell(5).setCellValue(policy.isActive() ? "Đang hoạt động" : "Không hoạt động");
            row.getCell(5).setCellStyle(dataStyle);

            // Cột Trạng thái xóa
            row.createCell(6).setCellValue(policy.isDeleted() ? "Đã xóa" : "Đang hiển thị");
            row.getCell(6).setCellStyle(dataStyle);
        }

        // Tự động điều chỉnh độ rộng cột theo nội dung
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Tạo OutputStream và ghi workbook vào đó
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }
}