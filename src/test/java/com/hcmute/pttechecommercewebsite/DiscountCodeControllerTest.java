package com.hcmute.pttechecommercewebsite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmute.pttechecommercewebsite.controller.DiscountCodeController;
import com.hcmute.pttechecommercewebsite.dto.DiscountCodeDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.service.DiscountCodeService;
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

import java.util.Date;
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
public class DiscountCodeControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private DiscountCodeService discountCodeService;

    @InjectMocks
    private DiscountCodeController discountCodeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(discountCodeController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Lấy tất cả mã giảm giá thành công")
    void getAllDiscountCodesSuccess() throws Exception {
        // Mock danh sách DiscountCodeDTO
        List<DiscountCodeDTO> mockDiscountCodes = List.of(
                new DiscountCodeDTO("1", "DISCOUNT10", "Giảm 10% cho mọi đơn hàng", "percentage", 10.0, null, null, null, "products", new Date(), new Date(), 100, 50, null, true, false, null),
                new DiscountCodeDTO("2", "DISCOUNT20", "Giảm 20% cho đơn hàng từ 500k", "percentage", 20.0, 500000.0, null, null, "products", new Date(), new Date(), 100, 20, null, true, false, null)
        );

        // Cập nhật mock service với tham số sortBy và sortOrder
        when(discountCodeService.getAllActiveDiscountCodes("code", "asc")).thenReturn(mockDiscountCodes);

        // Kiểm tra API
        mockMvc.perform(get("/api/discount-codes")
                        .param("sortBy", "code")  // Tham số sortBy
                        .param("sortOrder", "asc")) // Tham số sortOrder
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code", is("DISCOUNT10")))
                .andExpect(jsonPath("$[1].code", is("DISCOUNT20")));

        verify(discountCodeService, times(1)).getAllActiveDiscountCodes("code", "asc");
    }

    @Test
    @DisplayName("Lấy tất cả mã giảm giá khi không có dữ liệu")
    void getAllDiscountCodesNoContent() throws Exception {
        // Trả về danh sách rỗng khi không có mã giảm giá
        when(discountCodeService.getAllActiveDiscountCodes("code", "asc")).thenReturn(List.of());

        // Kiểm tra API khi không có dữ liệu
        mockMvc.perform(get("/api/discount-codes")
                        .param("sortBy", "code")  // Tham số sortBy
                        .param("sortOrder", "asc")) // Tham số sortOrder
                .andExpect(status().isNoContent());

        verify(discountCodeService, times(1)).getAllActiveDiscountCodes("code", "asc");
    }

    @Test
    @DisplayName("Lấy mã giảm giá theo ID thành công")
    void getDiscountCodeByIdSuccess() throws Exception {
        // Mock một DiscountCodeDTO
        DiscountCodeDTO mockDiscountCode = new DiscountCodeDTO("1", "DISCOUNT10", "Giảm 10% cho mọi đơn hàng", "percentage", 10.0, null, null, null, "products", new Date(), new Date(), 100, 50, null, true, false, null);

        when(discountCodeService.getDiscountCodeById("1")).thenReturn(Optional.of(mockDiscountCode));

        mockMvc.perform(get("/api/discount-codes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("DISCOUNT10")))
                .andExpect(jsonPath("$.description", is("Giảm 10% cho mọi đơn hàng")));

        verify(discountCodeService, times(1)).getDiscountCodeById("1");
    }

    @Test
    @DisplayName("Lấy mã giảm giá theo ID không tồn tại")
    void getDiscountCodeByIdNotFound() throws Exception {
        when(discountCodeService.getDiscountCodeById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/discount-codes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mã giảm giá với ID 99 không tồn tại"));

        verify(discountCodeService, times(1)).getDiscountCodeById("99");
    }

    @Test
    @DisplayName("Tìm kiếm mã giảm giá theo code thành công")
    void searchDiscountCodesSuccess() throws Exception {
        List<DiscountCodeDTO> mockDiscountCodes = List.of(
                new DiscountCodeDTO("1", "DISCOUNT10", "Giảm 10% cho mọi đơn hàng", "percentage", 10.0, null, null, null, "products", new Date(), new Date(), 100, 50, null, true, false, null)
        );

        when(discountCodeService.searchDiscountCodesByCode("DISCOUNT10")).thenReturn(mockDiscountCodes);

        mockMvc.perform(get("/api/discount-codes/search?keyword=DISCOUNT10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("DISCOUNT10")));

        verify(discountCodeService, times(1)).searchDiscountCodesByCode("DISCOUNT10");
    }

    @Test
    @DisplayName("Tìm kiếm mã giảm giá không có kết quả")
    void searchDiscountCodesNoContent() throws Exception {
        when(discountCodeService.searchDiscountCodesByCode("NONEXISTENT")).thenReturn(List.of());

        mockMvc.perform(get("/api/discount-codes/search?keyword=NONEXISTENT"))
                .andExpect(status().isNoContent());

        verify(discountCodeService, times(1)).searchDiscountCodesByCode("NONEXISTENT");
    }

    @Test
    @DisplayName("Thêm mã giảm giá thành công")
    void createDiscountCodeSuccess() throws Exception {
        DiscountCodeDTO newDiscountCode = new DiscountCodeDTO(null, "DISCOUNT30", "Giảm 30% cho đơn hàng trên 1 triệu", "percentage", 30.0, 1000000.0, null, null, "products", new Date(), new Date(), 100, 0, null, true, false, null);

        DiscountCodeDTO savedDiscountCode = new DiscountCodeDTO("3", "DISCOUNT30", "Giảm 30% cho đơn hàng trên 1 triệu", "percentage", 30.0, 1000000.0, null, null, "products", new Date(), new Date(), 100, 0, null, true, false, null);

        when(discountCodeService.createDiscountCode(any(DiscountCodeDTO.class))).thenReturn(savedDiscountCode);

        mockMvc.perform(post("/api/discount-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDiscountCode)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("3")))
                .andExpect(jsonPath("$.code", is("DISCOUNT30")))
                .andExpect(jsonPath("$.description", is("Giảm 30% cho đơn hàng trên 1 triệu")));

        verify(discountCodeService, times(1)).createDiscountCode(any(DiscountCodeDTO.class));
    }

    @Test
    @DisplayName("Chỉnh sửa mã giảm giá thành công")
    void updateDiscountCodeSuccess() throws Exception {
        DiscountCodeDTO updatedDiscountCode = new DiscountCodeDTO("1", "DISCOUNT15", "Giảm 15% cho đơn hàng", "percentage", 15.0, null, null, null, "products", new Date(), new Date(), 100, 10, null, true, false, null);

        when(discountCodeService.updateDiscountCode(eq("1"), any(DiscountCodeDTO.class))).thenReturn(updatedDiscountCode);

        mockMvc.perform(put("/api/discount-codes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDiscountCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Chỉnh sửa mã giảm giá thành công!")))
                .andExpect(jsonPath("$.data.id", is("1")))
                .andExpect(jsonPath("$.data.code", is("DISCOUNT15")))
                .andExpect(jsonPath("$.data.description", is("Giảm 15% cho đơn hàng")));

        verify(discountCodeService, times(1)).updateDiscountCode(eq("1"), any(DiscountCodeDTO.class));
    }

    @Test
    @DisplayName("Chỉnh sửa mã giảm giá thất bại khi không tìm thấy ID")
    void updateDiscountCodeNotFound() throws Exception {
        DiscountCodeDTO updatedDiscountCode = new DiscountCodeDTO("99", "DISCOUNT20", "Giảm 20% cho đơn hàng", "percentage", 20.0, null, null, null, "products", new Date(), new Date(), 100, 10, null, true, false, null);

        when(discountCodeService.updateDiscountCode(eq("99"), any(DiscountCodeDTO.class))).thenThrow(new ResourceNotFoundException("Mã giảm giá với ID 99 không tồn tại"));

        mockMvc.perform(put("/api/discount-codes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDiscountCode)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Mã giảm giá với ID 99 không tồn tại")));

        verify(discountCodeService, times(1)).updateDiscountCode(eq("99"), any(DiscountCodeDTO.class));
    }

    @Test
    @DisplayName("Xóa mã giảm giá thành công")
    void deleteDiscountCodeSuccess() throws Exception {
        doNothing().when(discountCodeService).deleteDiscountCode(eq("1"));

        mockMvc.perform(delete("/api/discount-codes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Bạn đã thực hiện xóa thành công mã giảm giá với ID: 1")));

        verify(discountCodeService, times(1)).deleteDiscountCode(eq("1"));
    }

    @Test
    @DisplayName("Xóa mã giảm giá thất bại khi không tìm thấy ID")
    void deleteDiscountCodeNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Mã giảm giá với ID 99 không tồn tại")).when(discountCodeService).deleteDiscountCode(eq("99"));

        mockMvc.perform(delete("/api/discount-codes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Mã giảm giá với ID 99 không tồn tại")));

        verify(discountCodeService, times(1)).deleteDiscountCode("99");
    }
}