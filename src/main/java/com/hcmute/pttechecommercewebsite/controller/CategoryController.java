package com.hcmute.pttechecommercewebsite.controller;

import com.hcmute.pttechecommercewebsite.dto.CategoryDTO;
import com.hcmute.pttechecommercewebsite.exception.MessageResponse;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Xem tất cả danh mục
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {

        List<CategoryDTO> categories = categoryService.getAllCategories(sortBy, sortOrder);
        if (categories.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // Xem tất cả danh mục
    @GetMapping("/no-delete")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesWithDeletedFalse(
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {

        List<CategoryDTO> categories = categoryService.getAllCategoriesWithDeletedFalse(sortBy, sortOrder);
        if (categories.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // Xem danh mục theo ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable String id) {
        return categoryService.getCategoryById(id)
                .map(categoryDTO -> new ResponseEntity<>(categoryDTO, HttpStatus.OK))
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục với ID " + id + " không tồn tại hoặc đã bị ẩn"));
    }

    // Lấy tất cả danh mục con theo parentCategoryId
    @GetMapping("/parent/{parentCategoryId}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesByParentId(@PathVariable String parentCategoryId) {
        List<CategoryDTO> categories = categoryService.getCategoriesByParentId(parentCategoryId);
        if (categories.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    // Tìm kiếm danh mục theo tên
    @GetMapping("/search")
    public ResponseEntity<List<CategoryDTO>> searchCategories(@RequestParam String keyword) {
        List<CategoryDTO> categories = categoryService.searchCategoriesByName(keyword);
        if (categories.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(categories);
    }

    // Thêm mới danh mục
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@ModelAttribute CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    // API tạo danh mục với thời gian lên lịch
    @PostMapping("/schedule-create")
    public ResponseEntity<CategoryDTO> scheduleCreateCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO scheduledCategory = categoryService.scheduleCreateCategory(categoryDTO);
        return new ResponseEntity<>(scheduledCategory, HttpStatus.CREATED);
    }

    // API tải ảnh lên
    @PostMapping("/upload-images")
    public ResponseEntity<MessageResponse> uploadAdImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = categoryService.uploadImage(file);
            return ResponseEntity.ok(new MessageResponse("Tải ảnh lên thành công", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi khi tải ảnh lên", e.getMessage()));
        }
    }

    // API xóa ảnh danh mục
    @DeleteMapping("/delete-image/{id}")
    public ResponseEntity<Object> deleteCategoryImage(@PathVariable String id) {
        try {
            categoryService.deleteCategoryImage(id);
            return new ResponseEntity<>(new MessageResponse("Xóa ảnh danh mục thành công!", id), HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi khi xóa ảnh danh mục", e.getMessage()));
        }
    }

    // Chỉnh sửa danh mục
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateCategory(@PathVariable String id, @ModelAttribute CategoryDTO categoryDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return new ResponseEntity<>(new MessageResponse("Chỉnh sửa danh mục thành công!", updatedCategory), HttpStatus.OK);
    }

    // API ẩn danh mục
    @PutMapping("/hide/{id}")
    public ResponseEntity<Object> hideCategory(@PathVariable String id) {
        CategoryDTO hiddenCategory = categoryService.hideCategory(id);
        return new ResponseEntity<>(new MessageResponse("Danh mục đã được ẩn thành công", hiddenCategory), HttpStatus.OK);
    }

    // API hiện danh mục
    @PutMapping("/show/{id}")
    public ResponseEntity<Object> showCategory(@PathVariable String id) {
        try {
            CategoryDTO categoryDTO = categoryService.showCategory(id);
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Không tìm thấy danh mục với ID: " + id, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi khi hiện danh mục", e.getMessage()));
        }
    }

    // Xóa danh mục
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(new MessageResponse("Bạn đã thực hiện xóa thành công danh mục với ID: " + id, id), HttpStatus.OK);
    }

    // API xuất danh mục ra file Excel
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportCategoriesToExcel(
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {

        try {
            ByteArrayOutputStream outputStream = categoryService.exportCategoriesToExcel(sortBy, sortOrder);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=categories.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Lỗi khi xuất file Excel: " + e.getMessage()).getBytes());
        }
    }
}