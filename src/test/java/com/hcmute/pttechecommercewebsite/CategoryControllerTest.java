package com.hcmute.pttechecommercewebsite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmute.pttechecommercewebsite.controller.CategoryController;
import com.hcmute.pttechecommercewebsite.dto.CategoryDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
        // Nếu không sử dụng @Autowired, bạn có thể khởi tạo ObjectMapper ở đây
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Lấy tất cả danh mục thành công")
    void getAllCategoriesSuccess() throws Exception {
        // Mock danh sách CategoryDTO
        List<CategoryDTO> mockCategories = List.of(
                new CategoryDTO("1", "Điện thoại", "Danh mục điện thoại", null, "image1.jpg", List.of("điện thoại", "smartphone"), false, true, null),
                new CategoryDTO("2", "Laptop", "Danh mục laptop", null, "image2.jpg", List.of("laptop", "máy tính"), false, true, null)
        );

        // Giả lập gọi service với tham số sortBy và sortOrder
        when(categoryService.getAllCategories("name", "desc")).thenReturn(mockCategories);

        // Thực hiện gọi API
        mockMvc.perform(get("/api/categories")
                        .param("sortBy", "name")        // Thêm tham số sortBy
                        .param("sortOrder", "desc"))    // Thêm tham số sortOrder
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))  // Kiểm tra số lượng danh mục
                .andExpect(jsonPath("$[0].name", is("Điện thoại")))  // Kiểm tra tên danh mục đầu tiên
                .andExpect(jsonPath("$[1].name", is("Laptop")));   // Kiểm tra tên danh mục thứ hai

        verify(categoryService, times(1)).getAllCategories("name", "desc");  // Kiểm tra gọi service một lần với tham số đúng
    }

    @Test
    @DisplayName("Lấy tất cả danh mục khi không có dữ liệu")
    void getAllCategoriesNoContent() throws Exception {
        // Giả lập service trả về danh sách rỗng
        when(categoryService.getAllCategories("name", "desc")).thenReturn(List.of());

        // Thực hiện gọi API
        mockMvc.perform(get("/api/categories")
                        .param("sortBy", "name")        // Thêm tham số sortBy
                        .param("sortOrder", "desc"))    // Thêm tham số sortOrder
                .andExpect(status().isNoContent());   // Kiểm tra HTTP status là NO_CONTENT

        verify(categoryService, times(1)).getAllCategories("name", "desc");  // Kiểm tra gọi service một lần với tham số đúng
    }

    @Test
    @DisplayName("Lấy danh mục theo ID thành công")
    void getCategoryByIdSuccess() throws Exception {
        // Mock một CategoryDTO
        CategoryDTO mockCategory = new CategoryDTO("1", "Điện thoại", "Danh mục điện thoại", null, "image1.jpg", List.of("điện thoại", "smartphone"), false, true, null);

        when(categoryService.getCategoryById("1")).thenReturn(Optional.of(mockCategory));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Điện thoại")))
                .andExpect(jsonPath("$.description", is("Danh mục điện thoại")));

        verify(categoryService, times(1)).getCategoryById("1");
    }

    @Test
    @DisplayName("Lấy danh mục theo ID không tồn tại")
    void getCategoryByIdNotFound() throws Exception {
        when(categoryService.getCategoryById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Danh mục với ID 99 không tồn tại"));

        verify(categoryService, times(1)).getCategoryById("99");
    }

    @Test
    @DisplayName("Lấy danh mục bị xóa (isDeleted = true)")
    void getCategoryDeleted() throws Exception {
        // Mock danh mục bị xóa
        when(categoryService.getCategoryById("3")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Danh mục với ID 3 không tồn tại"));

        verify(categoryService, times(1)).getCategoryById("3");
    }

    @Test
    @DisplayName("Lấy danh mục theo ID bị ẩn (isVisible = false)")
    void getCategoryByIdHidden() throws Exception {
        // Mock một CategoryDTO với isVisible = false
        CategoryDTO mockCategory = new CategoryDTO("2", "Laptop", "Danh mục laptop", null, "image2.jpg", List.of("laptop", "máy tính"), false, false, null);

        when(categoryService.getCategoryById("2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Danh mục với ID 2 không tồn tại"));

        verify(categoryService, times(1)).getCategoryById("2");
    }

    @Test
    @DisplayName("Lấy tất cả danh mục con theo parentCategoryId thành công")
    void getCategoriesByParentIdSuccess() throws Exception {
        // Mock danh sách CategoryDTO
        List<CategoryDTO> mockCategories = List.of(
                new CategoryDTO("3", "Điện thoại thông minh", "Danh mục điện thoại thông minh", "1", "image3.jpg", List.of("smartphone"), false, true, null),
                new CategoryDTO("4", "Máy tính xách tay", "Danh mục máy tính xách tay", "2", "image4.jpg", List.of("laptop"), false, true, null)
        );

        when(categoryService.getCategoriesByParentId("1")).thenReturn(mockCategories);

        mockMvc.perform(get("/api/categories/parent/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Điện thoại thông minh")))
                .andExpect(jsonPath("$[1].name", is("Máy tính xách tay")));

        verify(categoryService, times(1)).getCategoriesByParentId("1");
    }

    @Test
    @DisplayName("Lấy tất cả danh mục con theo parentCategoryId khi không có dữ liệu")
    void getCategoriesByParentIdNoContent() throws Exception {
        when(categoryService.getCategoriesByParentId("5")).thenReturn(List.of());

        mockMvc.perform(get("/api/categories/parent/5"))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).getCategoriesByParentId("5");
    }

    @Test
    @DisplayName("Tìm kiếm danh mục theo tên thành công")
    void searchCategoriesByNameSuccess() throws Exception {
        // Mock danh sách CategoryDTO
        List<CategoryDTO> mockCategories = List.of(
                new CategoryDTO("1", "Điện thoại", "Danh mục điện thoại", null, "image1.jpg", List.of("điện thoại", "smartphone"), false, true, null),
                new CategoryDTO("2", "Laptop", "Danh mục laptop", null, "image2.jpg", List.of("laptop", "máy tính"), false, true, null)
        );

        when(categoryService.searchCategoriesByName("Điện thoại")).thenReturn(mockCategories);

        mockMvc.perform(get("/api/categories/search")
                        .param("keyword", "Điện thoại"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Điện thoại")))
                .andExpect(jsonPath("$[1].name", is("Laptop")));

        verify(categoryService, times(1)).searchCategoriesByName("Điện thoại");
    }

    @Test
    @DisplayName("Tìm kiếm danh mục không có kết quả")
    void searchCategoriesByNameNoResult() throws Exception {
        // Mock trả về danh sách rỗng
        when(categoryService.searchCategoriesByName("Không tồn tại")).thenReturn(List.of());

        mockMvc.perform(get("/api/categories/search")
                        .param("keyword", "Không tồn tại"))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).searchCategoriesByName("Không tồn tại");
    }

    @Test
    @DisplayName("Tìm kiếm danh mục với từ khóa rỗng")
    void searchCategoriesByNameEmptyKeyword() throws Exception {
        // Mock trả về danh sách danh mục
        List<CategoryDTO> mockCategories = List.of(
                new CategoryDTO("1", "Điện thoại", "Danh mục điện thoại", null, "image1.jpg", List.of("điện thoại", "smartphone"), false, true, null),
                new CategoryDTO("2", "Laptop", "Danh mục laptop", null, "image2.jpg", List.of("laptop", "máy tính"), false, true, null)
        );

        when(categoryService.searchCategoriesByName("")).thenReturn(mockCategories);

        mockMvc.perform(get("/api/categories/search")
                        .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Điện thoại")))
                .andExpect(jsonPath("$[1].name", is("Laptop")));

        verify(categoryService, times(1)).searchCategoriesByName("");
    }

    @Test
    @DisplayName("Tìm kiếm danh mục với từ khóa không tìm thấy match")
    void searchCategoriesByNameNoMatch() throws Exception {
        // Mock không có danh mục nào khớp với từ khóa
        when(categoryService.searchCategoriesByName("Máy tính bảng")).thenReturn(List.of());

        mockMvc.perform(get("/api/categories/search")
                        .param("keyword", "Máy tính bảng"))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).searchCategoriesByName("Máy tính bảng");
    }

    @Test
    @DisplayName("Thêm danh mục thành công")
    void createCategorySuccess() throws Exception {
        CategoryDTO newCategory = new CategoryDTO(null, "Điện thoại", "Mô tả danh mục điện thoại", null, "image.jpg", List.of("smartphone", "mobile"), false, true, null);

        CategoryDTO savedCategory = new CategoryDTO("1", "Điện thoại", "Mô tả danh mục điện thoại", null, "image.jpg", List.of("smartphone", "mobile"), false, true, null);

        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(savedCategory);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("Điện thoại")))
                .andExpect(jsonPath("$.description", is("Mô tả danh mục điện thoại")));

        verify(categoryService, times(1)).createCategory(any(CategoryDTO.class));
    }

    @Test
    @DisplayName("Thêm danh mục thất bại khi dữ liệu không hợp lệ")
    void createCategoryBadRequest() throws Exception {
        CategoryDTO invalidCategory = new CategoryDTO(null, "", "Mô tả không hợp lệ", null, "", List.of(), false, true, null);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategory)))
                .andExpect(status().isBadRequest());

        verify(categoryService, times(0)).createCategory(any(CategoryDTO.class));
    }

    @Test
    @DisplayName("Chỉnh sửa danh mục thành công")
    void updateCategorySuccess() throws Exception {
        CategoryDTO updatedCategory = new CategoryDTO("1", "Điện thoại mới", "Mô tả cập nhật", null, "newimage.jpg", List.of("smartphone", "mobile"), false, true, null);

        when(categoryService.updateCategory(eq("1"), any(CategoryDTO.class))).thenReturn(updatedCategory);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Chỉnh sửa danh mục thành công!")))
                .andExpect(jsonPath("$.data.id", is("1")))
                .andExpect(jsonPath("$.data.name", is("Điện thoại mới")))
                .andExpect(jsonPath("$.data.description", is("Mô tả cập nhật")));

        verify(categoryService, times(1)).updateCategory(eq("1"), any(CategoryDTO.class));
    }

    @Test
    @DisplayName("Chỉnh sửa danh mục thất bại khi không tìm thấy ID")
    void updateCategoryNotFound() throws Exception {
        CategoryDTO updatedCategory = new CategoryDTO("99", "Điện thoại mới", "Mô tả cập nhật", null, "newimage.jpg", List.of("smartphone", "mobile"), false, true, null);

        when(categoryService.updateCategory(eq("99"), any(CategoryDTO.class))).thenThrow(new ResourceNotFoundException("Danh mục với ID 99 không tồn tại"));

        mockMvc.perform(put("/api/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCategory)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Danh mục với ID 99 không tồn tại")));

        verify(categoryService, times(1)).updateCategory(eq("99"), any(CategoryDTO.class));
    }

    @Test
    @DisplayName("Chỉnh sửa danh mục thất bại khi dữ liệu không hợp lệ")
    void updateCategoryBadRequest() throws Exception {
        CategoryDTO invalidCategory = new CategoryDTO("1", "", "Mô tả không hợp lệ", null, "", List.of(), false, true, null);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategory)))
                .andExpect(status().isBadRequest());

        verify(categoryService, times(0)).updateCategory(eq("1"), any(CategoryDTO.class));
    }

    @Test
    @DisplayName("Xóa danh mục thành công")
    void deleteBrandSuccess() throws Exception {
        doNothing().when(categoryService).deleteCategory(eq("1"));

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Bạn đã thực hiện xóa thành công danh mục với ID: 1")));

        verify(categoryService, times(1)).deleteCategory(eq("1"));
    }

    @Test
    @DisplayName("Xóa danh mục thất bại khi không tìm thấy ID")
    void deleteCategoryNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: 99")).when(categoryService).deleteCategory(eq("99"));

        mockMvc.perform(delete("/api/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Danh mục với ID 99 không tồn tại")));

        verify(categoryService, times(1)).deleteCategory("99");
    }
}