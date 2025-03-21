package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.CategoryDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.model.Category;
import com.hcmute.pttechecommercewebsite.repository.CategoryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

@Service
@Validated
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Thư mục lưu trữ hình ảnh
    private String uploadDir = "upload-images/categories";

    // URL công khai để truy cập hình ảnh
    private String uploadUrl = "http://localhost:8081/images/categories";

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Chuyển Entity thành DTO
    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategoryId() != null ? category.getParentCategoryId().toString() : null)
                .image(category.getImage())
                .tags(category.getTags())
                .isDeleted(category.isDeleted())
                .isActive(category.isActive())
                .build();
    }

    // Lấy tất cả danh mục không bị xóa và đang hiển thị
    public List<CategoryDTO> getAllCategories(String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        }

        // Sử dụng findByIsDeletedFalseAndIsActiveTrue với Sort
        List<Category> categories = categoryRepository.findByIsDeletedFalseAndIsActiveTrue(sort);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy tất cả danh mục không bị xóa
    public List<CategoryDTO> getAllCategoriesWithDeletedFalse(String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        }

        // Sử dụng findByIsDeletedFalseAndIsActiveTrue với Sort
        List<Category> categories = categoryRepository.findByIsDeletedFalse(sort);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy danh mục theo ID
    public Optional<CategoryDTO> getCategoryById(String id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.map(cat -> {
            if (!cat.isDeleted()) {
                return convertToDTO(cat);
            }
            return null;
        });
    }

    // Lấy tất cả danh mục con theo parentCategoryId
    public List<CategoryDTO> getCategoriesByParentId(String parentCategoryId) {
        ObjectId parentId = new ObjectId(parentCategoryId);
        List<Category> categories = categoryRepository.findByParentCategoryIdAndIsDeletedFalseAndIsActiveTrue(parentId);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Tìm kiếm theo tên danh mục
    public List<CategoryDTO> searchCategoriesByName(String keyword) {
        List<Category> categories = categoryRepository.findByNameContaining(keyword);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Thêm mới danh mục
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .parentCategoryId(categoryDTO.getParentCategoryId() != null ? new ObjectId(categoryDTO.getParentCategoryId()) : null)
                .image(categoryDTO.getImage())
                .tags(categoryDTO.getTags())
                .isDeleted(false)
                .isActive(true)
                .build();
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    // Tạo danh mục với thời gian lên lịch
    public CategoryDTO scheduleCreateCategory(CategoryDTO categoryDTO) {
        if (categoryDTO.getScheduledDate() != null && categoryDTO.getScheduledDate().before(new Date())) {
            throw new IllegalArgumentException("Thời gian phải trong tương lai.");
        }

        // Kiểm tra các trường thông tin bắt buộc
        if (categoryDTO.getName() == null || categoryDTO.getDescription() == null) {
            throw new IllegalArgumentException("Các trường thông tin bắt buộc không được để trống");
        }

        // Chuyển CategoryDTO thành Category entity
        Category newCategory = Category.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .parentCategoryId(categoryDTO.getParentCategoryId() != null ? new ObjectId(categoryDTO.getParentCategoryId()) : null)
                .image(categoryDTO.getImage())
                .tags(categoryDTO.getTags())
                .isDeleted(false)
                .isActive(false)
                .scheduledDate(categoryDTO.getScheduledDate())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        try {
            newCategory = categoryRepository.save(newCategory);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu danh mục vào cơ sở dữ liệu", e);
        }

        return convertToDTO(newCategory);
    }

    // Kiểm tra và kích hoạt danh mục khi đến thời gian lên lịch
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndActivateScheduledCategories() {
        Date now = new Date();

        List<Category> scheduledCategories = categoryRepository.findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(now);

        for (Category category : scheduledCategories) {
            category.setActive(true);
            category.setScheduledDate(null);
            category.setUpdatedAt(now);
            categoryRepository.save(category);
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

    // Xóa ảnh của danh mục
    public void deleteCategoryImage(String categoryId) throws IOException {
        Optional<Category> optionalCategory = categoryRepository.findByIdAndIsDeletedFalse(categoryId);
        if (!optionalCategory.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryId);
        }

        Category category = optionalCategory.get();
        String imagePath = category.getImage();

        if (imagePath != null && !imagePath.isEmpty()) {
            Path imageFilePath = Paths.get(uploadDir + File.separator + imagePath.substring(imagePath.lastIndexOf("/") + 1));
            File fileToDelete = imageFilePath.toFile();

            // Kiểm tra xem file có tồn tại không
            if (fileToDelete.exists()) {
                // Xóa file
                boolean isDeleted = fileToDelete.delete();
                if (!isDeleted) {
                    throw new IOException("Không thể xóa tệp ảnh: " + imagePath);
                }
            } else {
                throw new IOException("Tệp ảnh không tồn tại: " + imagePath);
            }
        } else {
            throw new ResourceNotFoundException("Danh mục này không có ảnh để xóa");
        }
    }


    // Chỉnh sửa danh mục
    public CategoryDTO updateCategory(String id, CategoryDTO categoryDTO) {
        Optional<Category> existingCategory = categoryRepository.findById(id);
        if (existingCategory.isPresent()) {
            Category category = existingCategory.get();
            category.setName(categoryDTO.getName());
            category.setDescription(categoryDTO.getDescription());
            category.setParentCategoryId(categoryDTO.getParentCategoryId() != null ? new ObjectId(categoryDTO.getParentCategoryId()) : null);
            category.setImage(categoryDTO.getImage());
            category.setTags(categoryDTO.getTags());
            category.setUpdatedAt(new Date());

            Category updatedCategory = categoryRepository.save(category);
            return convertToDTO(updatedCategory);
        } else {
            throw new ResourceNotFoundException("Danh mục với ID " + id + " không tồn tại");
        }
    }

    // Ẩn danh mục
    public CategoryDTO hideCategory(String id) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (!optionalCategory.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id);
        }

        Category category = optionalCategory.get();
        category.setActive(false);
        category.setUpdatedAt(new Date());
        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    // Hiện danh mục
    public CategoryDTO showCategory(String id) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (!optionalCategory.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id);
        }

        Category category = optionalCategory.get();
        category.setActive(true);
        category.setUpdatedAt(new Date());
        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    // Xóa danh mục
    public void deleteCategory(String id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            Category existingCategory = category.get();
            existingCategory.setDeleted(true);  // Xóa mềm
            categoryRepository.save(existingCategory);
        } else {
            throw new ResourceNotFoundException("Danh mục với ID " + id + " không tồn tại");
        }
    }

    // Phương thức xuất tất cả danh mục ra file Excel
    public ByteArrayOutputStream exportCategoriesToExcel(String sortBy, String sortOrder) throws IOException {
        // Lấy danh mục từ service với sắp xếp
        List<CategoryDTO> categories = getAllCategoriesWithDeletedFalse(sortBy, sortOrder);

        // Tạo workbook Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Categories");

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
        String[] columns = {"ID", "Tên danh mục", "Mô tả", "ID Danh mục cha", "Ảnh", "Tags", "Trạng thái hiển thị", "Trạng thái xóa"};
        for (int i = 0; i < columns.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Định dạng cho dữ liệu
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Thêm dữ liệu danh mục vào file Excel
        int rowNum = 1;
        for (CategoryDTO category : categories) {
            XSSFRow row = sheet.createRow(rowNum++);

            // Cột ID
            row.createCell(0).setCellValue(category.getId());
            row.getCell(0).setCellStyle(dataStyle);

            // Cột Tên danh mục
            row.createCell(1).setCellValue(category.getName());
            row.getCell(1).setCellStyle(dataStyle);

            // Cột Mô tả
            row.createCell(2).setCellValue(category.getDescription());
            row.getCell(2).setCellStyle(dataStyle);

            // Cột ID Danh mục cha
            row.createCell(3).setCellValue(category.getParentCategoryId() != null ? category.getParentCategoryId() : "Không có");
            row.getCell(3).setCellStyle(dataStyle);

            // Cột Ảnh
            row.createCell(4).setCellValue(category.getImage() != null ? category.getImage() : "Không có ảnh");
            row.getCell(4).setCellStyle(dataStyle);

            // Cột Tags
            row.createCell(5).setCellValue(category.getTags() != null ? String.join(", ", category.getTags()) : "Không có");
            row.getCell(5).setCellStyle(dataStyle);

            // Cột Trạng thái hiển thị
            row.createCell(6).setCellValue(category.isActive() ? "Đang hiển thị" : "Ẩn");
            row.getCell(6).setCellStyle(dataStyle);

            // Cột Trạng thái xóa
            row.createCell(7).setCellValue(category.isDeleted() ? "Đã xóa" : "Đang hiển thị");
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