package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.AdImageDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.model.AdImage;
import com.hcmute.pttechecommercewebsite.repository.AdImageRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
public class AdImageService {

    private final AdImageRepository adImageRepository;

    // Thư mục lưu trữ hình ảnh
    private String uploadDir = "upload-images/ad-images";

    // URL công khai để truy cập hình ảnh
    private String uploadUrl = "http://localhost:8081/images/ad-images";

    @Autowired
    public AdImageService(AdImageRepository adImageRepository) {
        this.adImageRepository = adImageRepository;
    }

    // Chuyển từ Entity sang DTO
    private AdImageDTO convertToDTO(AdImage adImage) {
        return AdImageDTO.builder()
                .id(adImage.getId())
                .title(adImage.getTitle())
                .image(adImage.getImage())
                .link(adImage.getLink())
                .description(adImage.getDescription())
                .startDate(adImage.getStartDate())
                .endDate(adImage.getEndDate())
                .isActive(adImage.isActive())
                .adType(adImage.getAdType())
                .isDeleted(adImage.isDeleted())
                .scheduledDate(adImage.getScheduledDate())
                .build();
    }

    // Lấy tất cả quảng cáo đang hoạt động
    public List<AdImageDTO> getAllActiveAdImages(String sortOrder) {
        List<AdImage> adImages;
        if ("desc".equalsIgnoreCase(sortOrder)) {
            adImages = adImageRepository.findByIsActiveTrueAndIsDeletedFalse(Sort.by(Sort.Order.desc("createdAt")));
        } else {
            adImages = adImageRepository.findByIsActiveTrueAndIsDeletedFalse(Sort.by(Sort.Order.asc("createdAt")));
        }
        return adImages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy tất cả quảng cáo khong bi xoa
    public List<AdImageDTO> getAllAdImages(String sortOrder) {
        List<AdImage> adImages;
        if ("desc".equalsIgnoreCase(sortOrder)) {
            adImages = adImageRepository.findByIsDeletedFalse(Sort.by(Sort.Order.desc("createdAt")));
        } else {
            adImages = adImageRepository.findByIsDeletedFalse(Sort.by(Sort.Order.asc("createdAt")));
        }
        return adImages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy quảng cáo theo ID
    public Optional<AdImageDTO> getAdImageById(String id) {
        Optional<AdImage> adImage = adImageRepository.findById(id);
        return adImage.map(this::convertToDTO);
    }

    // Tìm kiếm quảng cáo theo tiêu đề
    public List<AdImageDTO> searchAdImagesByTitle(String title) {
        List<AdImage> adImages = adImageRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(title);
        return adImages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Tạo mới quảng cáo
    public AdImageDTO createAdImage(AdImageDTO adImageDTO) {
        AdImage adImage = AdImage.builder()
                .title(adImageDTO.getTitle())
                .image(adImageDTO.getImage())
                .link(adImageDTO.getLink())
                .description(adImageDTO.getDescription())
                .startDate(adImageDTO.getStartDate())
                .endDate(adImageDTO.getEndDate())
                .adType(adImageDTO.getAdType())
                .isActive(true)
                .isDeleted(false)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        AdImage savedAdImage = adImageRepository.save(adImage);
        return convertToDTO(savedAdImage);
    }

    // Tạo quảng cáo với thời gian lên lịch
    public AdImageDTO scheduleCreateAdImage(AdImageDTO adImageDTO) {
        // Kiểm tra nếu thời gian lên lịch là trong tương lai
        if (adImageDTO.getScheduledDate() != null && adImageDTO.getScheduledDate().before(new Date())) {
            throw new IllegalArgumentException("Thời gian phải trong tương lai.");
        }

        // Kiểm tra xem adImageDTO có các trường hợp null hay không
        if (adImageDTO.getTitle() == null || adImageDTO.getImage() == null || adImageDTO.getLink() == null) {
            throw new IllegalArgumentException("Các trường thông tin bắt buộc không được để trống");
        }

        // Chuyển đổi AdImageDTO thành AdImage entity
        AdImage newAdImage = AdImage.builder()
                .title(adImageDTO.getTitle())
                .image(adImageDTO.getImage())
                .link(adImageDTO.getLink())
                .description(adImageDTO.getDescription())
                .startDate(adImageDTO.getStartDate())
                .endDate(adImageDTO.getEndDate())
                .adType(adImageDTO.getAdType())
                .isActive(false)
                .isDeleted(false)
                .scheduledDate(adImageDTO.getScheduledDate())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        try {
            newAdImage = adImageRepository.save(newAdImage);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu quảng cáo vào cơ sở dữ liệu", e);
        }

        return convertToDTO(newAdImage);
    }

    // Kiểm tra và kích hoạt quảng cáo khi đến thời gian lên lịch
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndActivateScheduledAdImages() {
        Date now = new Date();

        List<AdImage> scheduledAdImages = adImageRepository.findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(now);

        for (AdImage adImage : scheduledAdImages) {
            adImage.setActive(true);
            adImage.setScheduledDate(null);
            adImage.setUpdatedAt(now);
            adImageRepository.save(adImage);
        }
    }

    // Tạo một tên tệp duy nhất cho ảnh và lưu vào thư mục
    public String uploadAdImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        Path path = Paths.get(uploadDir + File.separator + fileName);

        Files.createDirectories(path.getParent());
        file.transferTo(path);

        return uploadUrl + "/" + fileName;
    }

    public void deleteFileAdImage(String adImageId) throws IOException {
        Optional<AdImage> optionalAdImage = adImageRepository.findById(adImageId);
        if (!optionalAdImage.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy quảng cáo với ID: " + adImageId);
        }

        AdImage adImage = optionalAdImage.get();
        String imagePath = adImage.getImage();

        if (imagePath != null && !imagePath.isEmpty()) {
            Path imageFilePath = Paths.get(uploadDir + File.separator + imagePath.substring(imagePath.lastIndexOf("/") + 1));
            File fileToDelete = imageFilePath.toFile();

            // Kiểm tra xem file ảnh có tồn tại không
            if (fileToDelete.exists()) {
                boolean isDeleted = fileToDelete.delete();
                if (!isDeleted) {
                    throw new IOException("Không thể xóa tệp ảnh: " + imagePath);
                }
            } else {
                throw new IOException("Tệp ảnh không tồn tại: " + imagePath);
            }
        } else {
            throw new ResourceNotFoundException("Quảng cáo này không có ảnh để xóa");
        }
    }

    // Cập nhật quảng cáo
    public AdImageDTO updateAdImage(String id, AdImageDTO adImageDTO) {
        Optional<AdImage> existingAdImage = adImageRepository.findById(id);
        if (existingAdImage.isPresent()) {
            AdImage adImage = existingAdImage.get();
            adImage.setTitle(adImageDTO.getTitle());
            adImage.setImage(adImageDTO.getImage());
            adImage.setLink(adImageDTO.getLink());
            adImage.setDescription(adImageDTO.getDescription());
            adImage.setStartDate(adImageDTO.getStartDate());
            adImage.setEndDate(adImageDTO.getEndDate());
            adImage.setAdType(adImageDTO.getAdType());
            adImage.setUpdatedAt(new Date());

            AdImage updatedAdImage = adImageRepository.save(adImage);
            return convertToDTO(updatedAdImage);
        } else {
            throw new ResourceNotFoundException("Quảng cáo với ID " + id + " không tồn tại");
        }
    }

    // Ẩn quảng cáo
    public AdImageDTO hideAdImage(String id) {
        Optional<AdImage> optionalAdImage = adImageRepository.findByIdAndIsDeletedFalse(id);
        if (!optionalAdImage.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy quảng cáo với ID: " + id);
        }

        AdImage adImage = optionalAdImage.get();
        adImage.setActive(false);  // Ẩn quảng cáo
        adImage.setUpdatedAt(new Date());
        adImage = adImageRepository.save(adImage);
        return convertToDTO(adImage);
    }

    // Hiện quảng cáo
    public AdImageDTO showAdImage(String id) {
        Optional<AdImage> optionalAdImage = adImageRepository.findByIdAndIsDeletedFalse(id);
        if (!optionalAdImage.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy quảng cáo với ID: " + id);
        }

        AdImage adImage = optionalAdImage.get();
        adImage.setActive(true);  // Hiện quảng cáo
        adImage.setUpdatedAt(new Date());
        adImage = adImageRepository.save(adImage);
        return convertToDTO(adImage);
    }

    // Xóa quảng cáo (xóa mềm)
    public void deleteAdImage(String id) {
        Optional<AdImage> adImage = adImageRepository.findById(id);
        if (adImage.isPresent()) {
            AdImage existingAdImage = adImage.get();
            existingAdImage.setDeleted(true);
            adImageRepository.save(existingAdImage);
        } else {
            throw new ResourceNotFoundException("Quảng cáo với ID " + id + " không tồn tại");
        }
    }

    // Phương thức xuất tất cả quảng cáo ra file Excel
    public ByteArrayOutputStream exportAdImagesToExcel() throws IOException {
        // Lấy tất cả quảng cáo không bị xóa
        List<AdImage> adImages = adImageRepository.findByIsDeletedFalse(Sort.by(Sort.Order.asc("createdAt")));

        // Tạo workbook Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Ad Images");

        // Định dạng chung cho workbook
        CellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Tạo dòng tiêu đề
        XSSFRow headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Tiêu đề", "Hình ảnh", "Liên kết", "Mô tả", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái hiển thị", "Trạng thái xóa", "Thời gian lên lịch"};
        for (int i = 0; i < columns.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Định dạng cho dữ liệu
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Thêm dữ liệu quảng cáo vào file Excel
        int rowNum = 1;
        for (AdImage adImage : adImages) {
            XSSFRow row = sheet.createRow(rowNum++);

            // Cột ID
            row.createCell(0).setCellValue(adImage.getId());
            row.getCell(0).setCellStyle(dataStyle);

            // Cột Tiêu đề
            row.createCell(1).setCellValue(adImage.getTitle());
            row.getCell(1).setCellStyle(dataStyle);

            // Cột Hình ảnh (URL hoặc đường dẫn)
            row.createCell(2).setCellValue(adImage.getImage());
            row.getCell(2).setCellStyle(dataStyle);

            // Cột Liên kết
            row.createCell(3).setCellValue(adImage.getLink());
            row.getCell(3).setCellStyle(dataStyle);

            // Cột Mô tả
            row.createCell(4).setCellValue(adImage.getDescription());
            row.getCell(4).setCellStyle(dataStyle);

            // Cột Ngày bắt đầu
            row.createCell(5).setCellValue(adImage.getStartDate() != null ? adImage.getStartDate().toString() : "");
            row.getCell(5).setCellStyle(dataStyle);

            // Cột Ngày kết thúc
            row.createCell(6).setCellValue(adImage.getEndDate() != null ? adImage.getEndDate().toString() : "");
            row.getCell(6).setCellStyle(dataStyle);

            // Cột Trạng thái hiển thị
            row.createCell(7).setCellValue(adImage.isActive() ? "Đang hiển thị" : "Ẩn");
            row.getCell(7).setCellStyle(dataStyle);

            // Cột Trạng thái xóa
            row.createCell(8).setCellValue(adImage.isDeleted() ? "Đã xóa" : "Đang hiển thị");
            row.getCell(8).setCellStyle(dataStyle);

            // Cột Thời gian lên lịch
            row.createCell(9).setCellValue(adImage.getScheduledDate() != null ? adImage.getScheduledDate().toString() : "");
            row.getCell(9).setCellStyle(dataStyle);
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