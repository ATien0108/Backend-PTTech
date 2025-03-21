package com.hcmute.pttechecommercewebsite;

import com.hcmute.pttechecommercewebsite.controller.BrandController;
import com.hcmute.pttechecommercewebsite.dto.BrandDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.service.BrandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
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
public class BrandControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BrandService brandService;

    @InjectMocks
    private BrandController brandController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(brandController).build();
    }

    // Lấy tất cả nhãn hiệu thành công
    @Test
    @DisplayName("Lấy tất cả nhãn hiệu thành công")
    void getAllBrandsSuccess() throws Exception {
        List<BrandDTO> mockBrands = List.of(
                new BrandDTO("1", "Apple", "Thương hiệu Apple", "apple-logo.jpg", "USA", "apple.com", false, true, null),
                new BrandDTO("2", "Samsung", "Thương hiệu Samsung", "samsung-logo.jpg", "Korea", "samsung.com", false, true, null)
        );

        when(brandService.getAllBrands(any(Sort.class))).thenReturn(mockBrands);

        mockMvc.perform(get("/api/brands")
                        .param("sortBy", "name")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Apple")))
                .andExpect(jsonPath("$[1].name", is("Samsung")));

        verify(brandService, times(1)).getAllBrands(any(Sort.class));
    }

    // Lấy tất cả nhãn hiệu khi không có dữ liệu
    @Test
    @DisplayName("Lấy tất cả nhãn hiệu khi không có dữ liệu")
    void getAllBrandsNoContent() throws Exception {
        when(brandService.getAllBrands(any(Sort.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isNoContent());

        verify(brandService, times(1)).getAllBrands(any(Sort.class));
    }

    // Lấy nhãn hiệu theo ID thành công
    @Test
    @DisplayName("Lấy nhãn hiệu theo ID thành công")
    void getBrandByIdSuccess() throws Exception {
        BrandDTO mockBrand = new BrandDTO("1", "Apple", "Thương hiệu Apple", "apple-logo.jpg", "USA", "apple.com", false, true, null);

        when(brandService.getBrandById("1")).thenReturn(Optional.of(mockBrand));

        mockMvc.perform(get("/api/brands/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Apple")))
                .andExpect(jsonPath("$.description", is("Thương hiệu Apple")));

        verify(brandService, times(1)).getBrandById("1");
    }

    // Lấy nhãn hiệu theo ID không tồn tại
    @Test
    @DisplayName("Lấy nhãn hiệu theo ID không tồn tại")
    void getBrandByIdNotFound() throws Exception {
        when(brandService.getBrandById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/brands/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nhãn hiệu với ID 99 không tồn tại"));

        verify(brandService, times(1)).getBrandById("99");
    }

    // Tìm kiếm nhãn hiệu theo từ khóa thành công
    @Test
    @DisplayName("Tìm kiếm nhãn hiệu theo từ khóa thành công")
    void searchBrandsSuccess() throws Exception {
        List<BrandDTO> mockBrands = List.of(
                new BrandDTO("1", "Apple", "Thương hiệu Apple", "apple-logo.jpg", "USA", "apple.com", false, true, null),
                new BrandDTO("2", "Samsung", "Thương hiệu Samsung", "samsung-logo.jpg", "Korea", "samsung.com", false, true, null)
        );

        when(brandService.searchBrandsByName("A")).thenReturn(mockBrands);

        mockMvc.perform(get("/api/brands/search").param("keyword", "A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Apple")))
                .andExpect(jsonPath("$[1].name", is("Samsung")));

        verify(brandService, times(1)).searchBrandsByName("A");
    }

    // Tìm kiếm nhãn hiệu theo từ khóa không có kết quả
    @Test
    @DisplayName("Tìm kiếm nhãn hiệu theo từ khóa không có kết quả")
    void searchBrandsNoContent() throws Exception {
        when(brandService.searchBrandsByName("Z")).thenReturn(List.of());

        mockMvc.perform(get("/api/brands/search").param("keyword", "Z"))
                .andExpect(status().isNoContent());

        verify(brandService, times(1)).searchBrandsByName("Z");
    }

    // Tìm kiếm nhãn hiệu với từ khóa null hoặc rỗng
    @Test
    @DisplayName("Tìm kiếm nhãn hiệu với từ khóa null hoặc rỗng")
    void searchBrandsInvalidKeyword() throws Exception {
        // Test với từ khóa rỗng
        mockMvc.perform(get("/api/brands/search").param("keyword", ""))
                .andExpect(status().isNoContent());

        verify(brandService, never()).searchBrandsByName(anyString());

        // Test với từ khóa null
        mockMvc.perform(get("/api/brands/search").param("keyword", (String) null))
                .andExpect(status().isNoContent());

        verify(brandService, never()).searchBrandsByName(anyString());
    }

    // Tạo thương hiệu mới thành công
    @Test
    @DisplayName("Tạo thương hiệu mới thành công")
    void createBrandSuccess() throws Exception {
        BrandDTO brandDTO = new BrandDTO("1", "Dell", "Thương hiệu máy tính Dell", "dell-logo.jpg", "USA", "dell.com", false, true, null);

        when(brandService.createBrand(any(BrandDTO.class))).thenReturn(brandDTO);

        mockMvc.perform(post("/api/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Dell\", \"description\": \"Thương hiệu máy tính Dell\", \"logo\": \"dell-logo.jpg\", \"country\": \"USA\", \"website\": \"dell.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Dell")))
                .andExpect(jsonPath("$.description", is("Thương hiệu máy tính Dell")));

        verify(brandService, times(1)).createBrand(any(BrandDTO.class));
    }

    // Tạo thương hiệu với dữ liệu thiếu
    @Test
    @DisplayName("Tạo thương hiệu với dữ liệu thiếu")
    void createBrandWithMissingData() throws Exception {
        mockMvc.perform(post("/api/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\", \"description\": \"Thương hiệu máy tính Dell\", \"logo\": \"dell-logo.jpg\", \"country\": \"USA\", \"website\": \"dell.com\"}"))
                .andExpect(status().isBadRequest());

        verify(brandService, times(0)).createBrand(any(BrandDTO.class));
    }

    // Chỉnh sửa thương hiệu thành công
    @Test
    @DisplayName("Chỉnh sửa thương hiệu thành công")
    void updateBrandSuccess() throws Exception {
        BrandDTO updatedBrand = new BrandDTO("1", "Dell", "Thương hiệu máy tính Dell", "dell-logo.jpg", "USA", "dell.com", false, true, null);

        when(brandService.updateBrand(eq("1"), any(BrandDTO.class))).thenReturn(updatedBrand);

        mockMvc.perform(put("/api/brands/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Dell\", \"description\": \"Thương hiệu máy tính Dell\", \"logo\": \"dell-logo.jpg\", \"country\": \"USA\", \"website\": \"dell.com\", \"isVisible\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Chỉnh sửa thương hiệu thành công!")));

        verify(brandService, times(1)).updateBrand(eq("1"), any(BrandDTO.class));
    }

    // Chỉnh sửa thương hiệu không tồn tại
    @Test
    @DisplayName("Chỉnh sửa thương hiệu không tồn tại")
    void updateBrandNotFound() throws Exception {
        when(brandService.updateBrand(eq("99"), any(BrandDTO.class))).thenThrow(new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: 99"));

        mockMvc.perform(put("/api/brands/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Dell\", \"description\": \"Thương hiệu máy tính Dell\", \"logo\": \"dell-logo.jpg\", \"country\": \"USA\", \"website\": \"dell.com\", \"isVisible\": true}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Không tìm thấy thương hiệu với ID: 99")));

        verify(brandService, times(1)).updateBrand(eq("99"), any(BrandDTO.class));
    }

    // Xóa thương hiệu thành công
    @Test
    @DisplayName("Xóa thương hiệu thành công")
    void deleteBrandSuccess() throws Exception {
        doNothing().when(brandService).deleteBrand(eq("1"));

        mockMvc.perform(delete("/api/brands/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Bạn đã thực hiện xóa thành công thương hiệu với ID: 1")));

        verify(brandService, times(1)).deleteBrand(eq("1"));
    }

    // Xóa thương hiệu không tồn tại
    @Test
    @DisplayName("Xóa thương hiệu không tồn tại")
    void deleteBrandNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: 99")).when(brandService).deleteBrand(eq("99"));

        mockMvc.perform(delete("/api/brands/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Không tìm thấy thương hiệu với ID: 99")));

        verify(brandService, times(1)).deleteBrand(eq("99"));
    }

    // Kiểm tra xóa thương hiệu đã bị xóa (isDeleted = true)
    @Test
    @DisplayName("Xóa thương hiệu đã bị xóa (isDeleted = true)")
    void deleteBrandAlreadyDeleted() throws Exception {
        doThrow(new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: 1")).when(brandService).deleteBrand(eq("1"));

        mockMvc.perform(delete("/api/brands/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Không tìm thấy thương hiệu với ID: 1")));

        verify(brandService, times(1)).deleteBrand(eq("1"));
    }
}