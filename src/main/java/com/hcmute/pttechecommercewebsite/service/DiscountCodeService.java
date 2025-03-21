package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.DiscountCodeDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.model.DiscountCode;
import com.hcmute.pttechecommercewebsite.repository.DiscountCodeRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.bson.types.ObjectId;
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
public class DiscountCodeService {

    private final DiscountCodeRepository discountCodeRepository;

    @Autowired
    public DiscountCodeService(DiscountCodeRepository discountCodeRepository) {
        this.discountCodeRepository = discountCodeRepository;
    }

    // Chuyển Entity thành DTO
    private DiscountCodeDTO convertToDTO(DiscountCode discountCode) {
        return DiscountCodeDTO.builder()
                .id(discountCode.getId())
                .code(discountCode.getCode())
                .description(discountCode.getDescription())
                .discountType(discountCode.getDiscountType())
                .discountValue(discountCode.getDiscountValue())
                .minimumPurchaseAmount(discountCode.getMinimumPurchaseAmount())
                .applicableCategories(discountCode.getApplicableCategories().stream()
                        .map(ObjectId::toString)
                        .collect(Collectors.toList()))
                .applicableProducts(discountCode.getApplicableProducts().stream()
                        .map(ObjectId::toString)
                        .collect(Collectors.toList()))
                .appliesTo(discountCode.getAppliesTo())
                .startDate(discountCode.getStartDate())
                .endDate(discountCode.getEndDate())
                .usageLimit(discountCode.getUsageLimit())
                .usageCount(discountCode.getUsageCount())
                .isActive(discountCode.isActive())
                .isDeleted(discountCode.isDeleted())
                .usedByUsers(discountCode.getUsedByUsers().stream()
                        .map(ObjectId::toString)
                        .collect(Collectors.toList()))
                .build();
    }

    // Xem tất cả mã giảm giá đang hoạt động
    public List<DiscountCodeDTO> getAllActiveDiscountCodes(String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();  // Sắp xếp giảm dần
        } else {
            sort = sort.ascending();  // Sắp xếp tăng dần
        }

        List<DiscountCode> discountCodes = discountCodeRepository.findByIsActiveTrueAndIsDeletedFalse(sort);

        return discountCodes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Xem tất cả mã giảm giá đang hoạt động
    public List<DiscountCodeDTO> getAllActiveDiscountCodesWithDeletedFalse(String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();  // Sắp xếp giảm dần
        } else {
            sort = sort.ascending();  // Sắp xếp tăng dần
        }

        List<DiscountCode> discountCodes = discountCodeRepository.findByIsDeletedFalse(sort);

        return discountCodes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Xem mã giảm giá theo ID
    public Optional<DiscountCodeDTO> getDiscountCodeById(String id) {
        Optional<DiscountCode> discountCode = discountCodeRepository.findById(id);
        return discountCode.map(this::convertToDTO);
    }

    // Tìm kiếm mã giảm giá theo code
    public List<DiscountCodeDTO> searchDiscountCodesByCode(String keyword) {
        List<DiscountCode> discountCodes = discountCodeRepository.findByCodeContaining(keyword);
        return discountCodes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Thêm mới mã giảm giá
    public DiscountCodeDTO createDiscountCode(DiscountCodeDTO discountCodeDTO) {
        List<ObjectId> usedByUsers = discountCodeDTO.getUsedByUsers().stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        List<ObjectId> applicableCategories = discountCodeDTO.getApplicableCategories().stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        List<ObjectId> applicableProducts = discountCodeDTO.getApplicableProducts().stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        DiscountCode discountCode = DiscountCode.builder()
                .code(discountCodeDTO.getCode())
                .description(discountCodeDTO.getDescription())
                .discountType(discountCodeDTO.getDiscountType())
                .discountValue(discountCodeDTO.getDiscountValue())
                .minimumPurchaseAmount(discountCodeDTO.getMinimumPurchaseAmount())
                .applicableCategories(applicableCategories)
                .applicableProducts(applicableProducts)
                .appliesTo(discountCodeDTO.getAppliesTo())
                .startDate(discountCodeDTO.getStartDate())
                .endDate(discountCodeDTO.getEndDate())
                .usageLimit(discountCodeDTO.getUsageLimit())
                .usageCount(0) // Khởi tạo số lần sử dụng ban đầu
                .usedByUsers(usedByUsers)
                .isActive(true)
                .isDeleted(false)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        DiscountCode savedDiscountCode = discountCodeRepository.save(discountCode);
        return convertToDTO(savedDiscountCode);
    }

    public DiscountCodeDTO scheduleCreateDiscountCode(DiscountCodeDTO discountCodeDTO) {
        // Kiểm tra nếu thời gian lên lịch là trong tương lai
        if (discountCodeDTO.getScheduledDate() != null && discountCodeDTO.getScheduledDate().before(new Date())) {
            throw new IllegalArgumentException("Thời gian phải trong tương lai.");
        }

        // Chuyển danh sách người dùng đã sử dụng mã giảm giá từ String về ObjectId
        List<ObjectId> usedByUsers = discountCodeDTO.getUsedByUsers().stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        List<ObjectId> applicableCategories = discountCodeDTO.getApplicableCategories().stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        List<ObjectId> applicableProducts = discountCodeDTO.getApplicableProducts().stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        // Tạo mã giảm giá mới
        DiscountCode discountCode = DiscountCode.builder()
                .code(discountCodeDTO.getCode())
                .description(discountCodeDTO.getDescription())
                .discountType(discountCodeDTO.getDiscountType())
                .discountValue(discountCodeDTO.getDiscountValue())
                .minimumPurchaseAmount(discountCodeDTO.getMinimumPurchaseAmount())
                .applicableCategories(applicableCategories)
                .applicableProducts(applicableProducts)
                .appliesTo(discountCodeDTO.getAppliesTo())
                .startDate(discountCodeDTO.getStartDate())
                .endDate(discountCodeDTO.getEndDate())
                .usageLimit(discountCodeDTO.getUsageLimit())
                .usageCount(0)
                .usedByUsers(usedByUsers)
                .isActive(false)
                .isDeleted(false)
                .scheduledDate(discountCodeDTO.getScheduledDate())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        DiscountCode savedDiscountCode = discountCodeRepository.save(discountCode);
        return convertToDTO(savedDiscountCode);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndActivateScheduledDiscountCodes() {
        Date now = new Date();

        List<DiscountCode> scheduledDiscountCodes = discountCodeRepository.findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(now);

        for (DiscountCode discountCode : scheduledDiscountCodes) {
            discountCode.setActive(true);
            discountCode.setScheduledDate(null);
            discountCode.setUpdatedAt(now);
            discountCodeRepository.save(discountCode);
        }
    }

    // Chỉnh sửa mã giảm giá
    public DiscountCodeDTO updateDiscountCode(String id, DiscountCodeDTO discountCodeDTO) {
        Optional<DiscountCode> existingDiscountCode = discountCodeRepository.findById(id);
        if (existingDiscountCode.isPresent()) {
            DiscountCode discountCode = existingDiscountCode.get();
            discountCode.setCode(discountCodeDTO.getCode());
            discountCode.setDescription(discountCodeDTO.getDescription());
            discountCode.setDiscountType(discountCodeDTO.getDiscountType());
            discountCode.setDiscountValue(discountCodeDTO.getDiscountValue());
            discountCode.setMinimumPurchaseAmount(discountCodeDTO.getMinimumPurchaseAmount());
            discountCode.setAppliesTo(discountCodeDTO.getAppliesTo());
            discountCode.setStartDate(discountCodeDTO.getStartDate());
            discountCode.setEndDate(discountCodeDTO.getEndDate());
            discountCode.setUsageLimit(discountCodeDTO.getUsageLimit());
            discountCode.setUsageCount(discountCodeDTO.getUsageCount());

            if (discountCodeDTO.getUsedByUsers() != null) {
                List<ObjectId> usedByUsers = discountCodeDTO.getUsedByUsers().stream()
                        .map(ObjectId::new)
                        .collect(Collectors.toList());
                discountCode.setUsedByUsers(usedByUsers);
            }

            if (discountCodeDTO.getApplicableCategories() != null) {
                List<ObjectId> applicableCategories = discountCodeDTO.getApplicableCategories().stream()
                        .map(ObjectId::new)
                        .collect(Collectors.toList());
                discountCode.setApplicableCategories(applicableCategories);
            }

            if (discountCodeDTO.getApplicableProducts() != null) {
                List<ObjectId> applicableProducts = discountCodeDTO.getApplicableProducts().stream()
                        .map(ObjectId::new)
                        .collect(Collectors.toList());
                discountCode.setApplicableProducts(applicableProducts);
            }

            discountCode.setUpdatedAt(new Date());

            DiscountCode updatedDiscountCode = discountCodeRepository.save(discountCode);
            return convertToDTO(updatedDiscountCode);
        } else {
            throw new ResourceNotFoundException("Mã giảm giá với ID " + id + " không tồn tại");
        }
    }

    // Hide discount code
    public DiscountCodeDTO hideDiscountCode(String id) {
        Optional<DiscountCode> optionalDiscountCode = discountCodeRepository.findById(id);
        if (!optionalDiscountCode.isPresent()) {
            throw new ResourceNotFoundException("Mã giảm giá có ID " + id + " không tìm thấy");
        }

        DiscountCode discountCode = optionalDiscountCode.get();
        discountCode.setActive(false);
        discountCode.setUpdatedAt(new Date());
        discountCode = discountCodeRepository.save(discountCode);
        return convertToDTO(discountCode);
    }

    // Show discount code
    public DiscountCodeDTO showDiscountCode(String id) {
        Optional<DiscountCode> optionalDiscountCode = discountCodeRepository.findById(id);
        if (!optionalDiscountCode.isPresent()) {
            throw new ResourceNotFoundException("Mã giảm giá có ID " + id + " không tìm thấy");
        }

        DiscountCode discountCode = optionalDiscountCode.get();
        discountCode.setActive(true);
        discountCode.setUpdatedAt(new Date());
        discountCode = discountCodeRepository.save(discountCode);
        return convertToDTO(discountCode);
    }

    // Xóa mã giảm giá (xóa mềm)
    public void deleteDiscountCode(String id) {
        Optional<DiscountCode> discountCode = discountCodeRepository.findById(id);
        if (discountCode.isPresent()) {
            DiscountCode existingDiscountCode = discountCode.get();
            existingDiscountCode.setDeleted(true);
            discountCodeRepository.save(existingDiscountCode);
        } else {
            throw new ResourceNotFoundException("Mã giảm giá với ID " + id + " không tồn tại");
        }
    }

    // Phương thức xuất tất cả mã giảm giá ra file Excel
    public ByteArrayOutputStream exportDiscountCodesToExcel(String sortBy, String sortOrder) throws IOException {
        // Lấy danh sách các mã giảm giá
        List<DiscountCodeDTO> discountCodes = getAllActiveDiscountCodesWithDeletedFalse(sortBy, sortOrder);

        // Tạo workbook Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("DiscountCodes");

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
        String[] columns = {"ID", "Mã giảm giá", "Mô tả", "Loại giảm giá", "Giá trị giảm", "Số tiền mua tối thiểu", "Ngày bắt đầu", "Ngày kết thúc", "Số lượt sử dụng", "Số lượt đã dùng", "Danh mục áp dụng", "Sản phẩm áp dụng", "Người dùng đã sử dụng", "Trạng thái hoạt động", "Trạng thái xóa"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Định dạng cho dữ liệu
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Thêm dữ liệu mã giảm giá vào file Excel
        int rowNum = 1;
        for (DiscountCodeDTO discountCode : discountCodes) {
            Row row = sheet.createRow(rowNum++);

            // Cột ID
            row.createCell(0).setCellValue(discountCode.getId());
            row.getCell(0).setCellStyle(dataStyle);

            // Cột Mã giảm giá
            row.createCell(1).setCellValue(discountCode.getCode());
            row.getCell(1).setCellStyle(dataStyle);

            // Cột Mô tả
            row.createCell(2).setCellValue(discountCode.getDescription() != null ? discountCode.getDescription() : "Không có");
            row.getCell(2).setCellStyle(dataStyle);

            // Cột Loại giảm giá
            row.createCell(3).setCellValue(discountCode.getDiscountType() != null ? discountCode.getDiscountType() : "Không có");
            row.getCell(3).setCellStyle(dataStyle);

            // Cột Giá trị giảm
            row.createCell(4).setCellValue(discountCode.getDiscountValue());
            row.getCell(4).setCellStyle(dataStyle);

            // Cột Số tiền mua tối thiểu
            row.createCell(5).setCellValue(discountCode.getMinimumPurchaseAmount() != null ? discountCode.getMinimumPurchaseAmount() : 0);
            row.getCell(5).setCellStyle(dataStyle);

            // Cột Ngày bắt đầu
            row.createCell(6).setCellValue(discountCode.getStartDate() != null ? discountCode.getStartDate().toString() : "Không có");
            row.getCell(6).setCellStyle(dataStyle);

            // Cột Ngày kết thúc
            row.createCell(7).setCellValue(discountCode.getEndDate() != null ? discountCode.getEndDate().toString() : "Không có");
            row.getCell(7).setCellStyle(dataStyle);

            // Cột Số lượt sử dụng
            row.createCell(8).setCellValue(discountCode.getUsageLimit());
            row.getCell(8).setCellStyle(dataStyle);

            // Cột Số lượt đã dùng
            row.createCell(9).setCellValue(discountCode.getUsageCount());
            row.getCell(9).setCellStyle(dataStyle);

            // Cột Danh mục áp dụng (Applicable Categories)
            String applicableCategories = discountCode.getApplicableCategories() != null ? String.join(", ", discountCode.getApplicableCategories()) : "Không có";
            row.createCell(10).setCellValue(applicableCategories);
            row.getCell(10).setCellStyle(dataStyle);

            // Cột Sản phẩm áp dụng (Applicable Products)
            String applicableProducts = discountCode.getApplicableProducts() != null ? String.join(", ", discountCode.getApplicableProducts()) : "Không có";
            row.createCell(11).setCellValue(applicableProducts);
            row.getCell(11).setCellStyle(dataStyle);

            // Cột Người dùng đã sử dụng (Used By Users)
            String usedByUsers = discountCode.getUsedByUsers() != null ? String.join(", ", discountCode.getUsedByUsers()) : "Không có";
            row.createCell(12).setCellValue(usedByUsers);
            row.getCell(12).setCellStyle(dataStyle);

            // Cột Trạng thái hoạt động
            row.createCell(13).setCellValue(discountCode.isActive() ? "Đang hoạt động" : "Không hoạt động");
            row.getCell(10).setCellStyle(dataStyle);

            // Cột Trạng thái xóa
            row.createCell(14).setCellValue(discountCode.isDeleted() ? "Đã xóa" : "Đang hiển thị");
            row.getCell(11).setCellStyle(dataStyle);
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