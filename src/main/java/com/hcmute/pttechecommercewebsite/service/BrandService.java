package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.BrandDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.model.Brand;
import com.hcmute.pttechecommercewebsite.repository.BrandRepository;
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
public class BrandService {

    private final BrandRepository brandRepository;

    // Thư mục lưu trữ hình ảnh
    private String uploadDir = "upload-images/brands";

    // URL công khai để truy cập hình ảnh
    private String uploadUrl = "http://localhost:8081/images/brands";

    @Autowired
    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    // Chuyển Entity thành DTO
    private BrandDTO convertToDTO(Brand brand) {
        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .logo(brand.getLogo())
                .country(brand.getCountry())
                .website(brand.getWebsite())
                .isDeleted(brand.isDeleted())
                .isActive(brand.isActive())
                .scheduledDate(brand.getScheduledDate())
                .build();
    }

    // Lấy tất cả thương hiệu không bị xóa và đang hiển thị
    public List<BrandDTO> getAllBrands(Sort sort) {
        List<Brand> brands = brandRepository.findByIsDeletedFalseAndIsActiveTrue(sort);
        return brands.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy tất cả thương hiệu không bị xóa
    public List<BrandDTO> getAllBrandsWithDeletedFalse(Sort sort) {
        List<Brand> brands = brandRepository.findByIsDeletedFalse(sort);
        return brands.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy thương hiệu theo ID
    public Optional<BrandDTO> getBrandById(String id) {
        Optional<Brand> brand = brandRepository.findByIdAndIsDeletedFalse(id);
        return brand.map(this::convertToDTO);
    }

    // Tìm kiếm theo tên thương hiệu
    public List<BrandDTO> searchBrandsByName(String keyword) {
        List<Brand> brands = brandRepository.findByNameContaining(keyword);
        return brands.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Tạo thương hiệu mới
    public BrandDTO createBrand(BrandDTO brandDTO) {
        Brand newBrand = Brand.builder()
                .name(brandDTO.getName())
                .description(brandDTO.getDescription())
                .logo(brandDTO.getLogo())
                .country(brandDTO.getCountry())
                .website(brandDTO.getWebsite())
                .isDeleted(false)
                .isActive(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        newBrand = brandRepository.save(newBrand);
        return convertToDTO(newBrand);
    }

    // Tạo thương hiệu với thời gian lên lịch
    public BrandDTO scheduleCreateBrand(BrandDTO brandDTO) {
        // Kiểm tra nếu thời gian lên lịch là trong tương lai
        if (brandDTO.getScheduledDate() != null && brandDTO.getScheduledDate().before(new Date())) {
            throw new IllegalArgumentException("Thời gian phải trong tương lai.");
        }

        // Kiểm tra xem brandDTO có các trường hợp null hay không
        if (brandDTO.getName() == null || brandDTO.getDescription() == null || brandDTO.getCountry() == null || brandDTO.getWebsite() == null) {
            throw new IllegalArgumentException("Các trường thông tin bắt buộc không được để trống");
        }

        // Chuyển đổi BrandDTO thành Brand entity
        Brand newBrand = Brand.builder()
                .name(brandDTO.getName())
                .description(brandDTO.getDescription())
                .logo(brandDTO.getLogo())
                .country(brandDTO.getCountry())
                .website(brandDTO.getWebsite())
                .isDeleted(false)
                .isActive(false)
                .scheduledDate(brandDTO.getScheduledDate())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        try {
            newBrand = brandRepository.save(newBrand);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu thương hiệu vào cơ sở dữ liệu", e);
        }

        return convertToDTO(newBrand);
    }

    // Kiểm tra và kích hoạt thương hiệu khi đến thời gian lên lịch
    @Scheduled(fixedRate = 60000) // Kiểm tra mỗi phút
    @Transactional
    public void checkAndCreateScheduledBrands() {
        Date now = new Date();

        List<Brand> scheduledBrands = brandRepository.findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(now);

        for (Brand brand : scheduledBrands) {
            brand.setActive(true);
            brand.setScheduledDate(null);
            brand.setUpdatedAt(now);
            brandRepository.save(brand);
        }
    }

    // Tạo một tên tệp duy nhất cho ảnh và lưu vào thư mục
    public String uploadImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        Path path = Paths.get(uploadDir + File.separator + fileName);

        Files.createDirectories(path.getParent());
        file.transferTo(path);

        return uploadUrl + "/" + fileName;
    }

    // Xóa ảnh của thương hiệu
    public void deleteBrandImage(String brandId) throws IOException {
        Optional<Brand> optionalBrand = brandRepository.findByIdAndIsDeletedFalse(brandId);
        if (!optionalBrand.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + brandId);
        }

        Brand brand = optionalBrand.get();
        String logoPath = brand.getLogo();

        if (logoPath != null && !logoPath.isEmpty()) {
            Path imagePath = Paths.get(uploadDir + File.separator + logoPath.substring(logoPath.lastIndexOf("/") + 1));
            File fileToDelete = imagePath.toFile();

            // Kiểm tra xem tệp có tồn tại không
            if (fileToDelete.exists()) {
                // Xóa tệp
                boolean isDeleted = fileToDelete.delete();
                if (!isDeleted) {
                    throw new IOException("Không thể xóa tệp ảnh: " + logoPath);
                }
                brandRepository.save(brand);
            } else {
                throw new IOException("Tệp ảnh không tồn tại: " + logoPath);
            }
        } else {
            throw new ResourceNotFoundException("Thương hiệu này không có ảnh để xóa");
        }
    }

    // Chỉnh sửa thương hiệu
    public BrandDTO updateBrand(String id, BrandDTO brandDTO) {
        Optional<Brand> optionalBrand = brandRepository.findByIdAndIsDeletedFalse(id);
        if (!optionalBrand.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id);
        }

        Brand brand = optionalBrand.get();

        // Cập nhật các trường nếu có thay đổi
        brand.setName(brandDTO.getName());
        brand.setDescription(brandDTO.getDescription());
        brand.setLogo(brandDTO.getLogo());
        brand.setCountry(brandDTO.getCountry());
        brand.setWebsite(brandDTO.getWebsite());
        brand.setUpdatedAt(new Date());

        brand = brandRepository.save(brand);

        return convertToDTO(brand);
    }

    // Ẩn thương hiệu
    public BrandDTO hideBrand(String id) {
        Optional<Brand> optionalBrand = brandRepository.findByIdAndIsDeletedFalse(id);
        if (!optionalBrand.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id);
        }

        Brand brand = optionalBrand.get();
        brand.setActive(false);
        brand.setUpdatedAt(new Date());
        brand = brandRepository.save(brand);
        return convertToDTO(brand);
    }

    // Hiện thương hiệu
    public BrandDTO showBrand(String id) {
        Optional<Brand> optionalBrand = brandRepository.findByIdAndIsDeletedFalse(id);
        if (!optionalBrand.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id);
        }

        Brand brand = optionalBrand.get();
        brand.setActive(true);
        brand.setUpdatedAt(new Date());
        brand = brandRepository.save(brand);
        return convertToDTO(brand);
    }

    // Xóa thương hiệu (xóa mềm)
    public void deleteBrand(String id) {
        Optional<Brand> optionalBrand = brandRepository.findByIdAndIsDeletedFalse(id);
        if (!optionalBrand.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id);
        }

        Brand brand = optionalBrand.get();
        brand.setDeleted(true);
        brand.setUpdatedAt(new Date());
        brandRepository.save(brand);
    }

    // Phương thức xuất tất cả thương hiệu ra file Excel
    public ByteArrayOutputStream exportBrandsToExcel() throws IOException {
        List<BrandDTO> brands = getAllBrandsWithDeletedFalse(Sort.by("name"));

        // Tạo workbook Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Brands");

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
        String[] columns = {"ID", "Tên", "Mô tả", "Logo", "Quốc gia", "Website", "Trạng thái hiển thị", "Trạng thái xóa"};
        for (int i = 0; i < columns.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Định dạng cho dữ liệu
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Thêm dữ liệu thương hiệu vào file Excel
        int rowNum = 1;
        for (BrandDTO brand : brands) {
            XSSFRow row = sheet.createRow(rowNum++);

            // Cột ID
            row.createCell(0).setCellValue(brand.getId());
            row.getCell(0).setCellStyle(dataStyle);

            // Cột Tên
            row.createCell(1).setCellValue(brand.getName());
            row.getCell(1).setCellStyle(dataStyle);

            // Cột Mô tả
            row.createCell(2).setCellValue(brand.getDescription());
            row.getCell(2).setCellStyle(dataStyle);

            // Cột Logo
            row.createCell(3).setCellValue(brand.getLogo());
            row.getCell(3).setCellStyle(dataStyle);

            // Cột Quốc gia
            row.createCell(4).setCellValue(brand.getCountry());
            row.getCell(4).setCellStyle(dataStyle);

            // Cột Website
            row.createCell(5).setCellValue(brand.getWebsite());
            row.getCell(5).setCellStyle(dataStyle);

            // Cột Trạng thái hiển thị
            row.createCell(6).setCellValue(brand.isActive() ? "Đang hiển thị" : "Ẩn");
            row.getCell(6).setCellStyle(dataStyle);

            // Cột Trạng thái xóa
            row.createCell(7).setCellValue(brand.isDeleted() ? "Đã xóa" : "Đang hiển thị");
            row.getCell(7).setCellStyle(dataStyle);
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