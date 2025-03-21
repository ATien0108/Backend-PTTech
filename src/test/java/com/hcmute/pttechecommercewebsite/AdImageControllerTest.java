package com.hcmute.pttechecommercewebsite;

import com.hcmute.pttechecommercewebsite.controller.AdImageController;
import com.hcmute.pttechecommercewebsite.dto.AdImageDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.service.AdImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdImageControllerTest {

    @InjectMocks
    private AdImageController adImageController;

    @Mock
    private AdImageService adImageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Kiểm tra lấy tất cả quảng cáo đang hoạt động khi có dữ liệu")
    void testGetAllActiveAdImages_whenDataExists() {
        List<AdImageDTO> mockAdImages = List.of(
                new AdImageDTO("1", "Ad 1", "http://image1.jpg", null, "Description 1", new Date(), null, true, "main", false, null),
                new AdImageDTO("2", "Ad 2", "http://image2.jpg", null, "Description 2", new Date(), null, true, "secondary", false, null)
        );
        when(adImageService.getAllActiveAdImages("desc")).thenReturn(mockAdImages);

        ResponseEntity<List<AdImageDTO>> response = adImageController.getAllActiveAdImages("desc");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(adImageService, times(1)).getAllActiveAdImages("desc");
    }

    @Test
    @DisplayName("Kiểm tra lấy tất cả quảng cáo đang hoạt động khi không có dữ liệu")
    void testGetAllActiveAdImages_whenNoDataExists() {
        when(adImageService.getAllActiveAdImages("desc")).thenReturn(new ArrayList<>());

        ResponseEntity<List<AdImageDTO>> response = adImageController.getAllActiveAdImages("desc");

        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(adImageService, times(1)).getAllActiveAdImages("desc");
    }

    @Test
    @DisplayName("Kiểm tra lấy quảng cáo theo ID khi ID tồn tại")
    void testGetAdImageById_whenIdExists() {
        AdImageDTO mockAdImage = new AdImageDTO("1", "Ad 1", "http://image1.jpg", null, "Description 1", new Date(), null, true, "main", false, null);
        when(adImageService.getAdImageById("1")).thenReturn(Optional.of(mockAdImage));

        ResponseEntity<AdImageDTO> response = adImageController.getAdImageById("1");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Ad 1", response.getBody().getTitle());
        verify(adImageService, times(1)).getAdImageById("1");
    }

    @Test
    @DisplayName("Kiểm tra lấy quảng cáo theo ID khi ID không tồn tại")
    void testGetAdImageById_whenIdDoesNotExist() {
        when(adImageService.getAdImageById("1")).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> adImageController.getAdImageById("1"));
        assertEquals("Quảng cáo với ID 1 không tồn tại", exception.getMessage());
        verify(adImageService, times(1)).getAdImageById("1");
    }

    @Test
    @DisplayName("Kiểm tra tìm quảng cáo theo tiêu đề khi có dữ liệu")
    void testSearchAdImagesByTitle_whenDataExists() {
        List<AdImageDTO> mockAdImages = List.of(
                new AdImageDTO("1", "Ad with Title", "http://image1.jpg", null, "Description 1", new Date(), null, true, "main", false, null)
        );
        when(adImageService.searchAdImagesByTitle("Title")).thenReturn(mockAdImages);

        ResponseEntity<List<AdImageDTO>> response = adImageController.searchAdImages("Title");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(adImageService, times(1)).searchAdImagesByTitle("Title");
    }

    @Test
    @DisplayName("Kiểm tra tìm quảng cáo theo tiêu đề khi không có dữ liệu")
    void testSearchAdImagesByTitle_whenNoDataExists() {
        when(adImageService.searchAdImagesByTitle("NonExistent")).thenReturn(new ArrayList<>());

        ResponseEntity<List<AdImageDTO>> response = adImageController.searchAdImages("NonExistent");

        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(adImageService, times(1)).searchAdImagesByTitle("NonExistent");
    }

    @Test
    @DisplayName("Kiểm tra tạo quảng cáo mới khi dữ liệu hợp lệ")
    void testCreateAdImage_whenValidData() {
        AdImageDTO request = new AdImageDTO(null, "New Ad", "http://newimage.jpg", null, "Description", new Date(), null, true, "main", false, null);
        AdImageDTO savedAdImage = new AdImageDTO("1", "New Ad", "http://newimage.jpg", null, "Description", new Date(), null, true, "main", false, null);
        when(adImageService.createAdImage(request)).thenReturn(savedAdImage);

        ResponseEntity<AdImageDTO> response = adImageController.createAdImage(request);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("1", response.getBody().getId());
        verify(adImageService, times(1)).createAdImage(request);
    }

    @Test
    @DisplayName("Kiểm tra tạo quảng cáo mới khi dữ liệu không hợp lệ")
    void testCreateAdImage_whenInvalidData() {
        AdImageDTO request = new AdImageDTO(null, null, null, null, null, new Date(), null, true, "main", false, null);

        when(adImageService.createAdImage(request)).thenThrow(new IllegalArgumentException("Dữ liệu không hợp lệ"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> adImageController.createAdImage(request));
        assertEquals("Dữ liệu không hợp lệ", exception.getMessage());
        verify(adImageService, times(1)).createAdImage(request);
    }

    @Test
    @DisplayName("Kiểm tra cập nhật quảng cáo khi dữ liệu hợp lệ")
    void testUpdateAdImage_whenValidData() {
        AdImageDTO request = new AdImageDTO(null, "Updated Ad", "http://updatedimage.jpg", null, "Updated Description", new Date(), null, true, "main", false, null);
        AdImageDTO updatedAdImage = new AdImageDTO("1", "Updated Ad", "http://updatedimage.jpg", null, "Updated Description", new Date(), null, true, "main", false, null);
        when(adImageService.updateAdImage("1", request)).thenReturn(updatedAdImage);

        ResponseEntity<Object> response = adImageController.updateAdImage("1", request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Cập nhật quảng cáo thành công!"));
        verify(adImageService, times(1)).updateAdImage("1", request);
    }

    @Test
    @DisplayName("Kiểm tra cập nhật quảng cáo khi ID không tồn tại")
    void testUpdateAdImage_whenIdDoesNotExist() {
        AdImageDTO request = new AdImageDTO(null, "Updated Ad", "http://updatedimage.jpg", null, "Updated Description", new Date(), null, true, "main", false, null);

        when(adImageService.updateAdImage("999", request)).thenThrow(new ResourceNotFoundException("Quảng cáo với ID 999 không tồn tại"));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> adImageController.updateAdImage("999", request));
        assertEquals("Quảng cáo với ID 999 không tồn tại", exception.getMessage());
        verify(adImageService, times(1)).updateAdImage("999", request);
    }

    @Test
    @DisplayName("Kiểm tra xóa quảng cáo khi ID tồn tại")
    void testDeleteAdImage_whenIdExists() {
        doNothing().when(adImageService).deleteAdImage("1");

        ResponseEntity<Object> response = adImageController.deleteAdImage("1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Bạn đã thực hiện xóa thành công quảng cáo với ID: 1"));
        verify(adImageService, times(1)).deleteAdImage("1");
    }

    @Test
    @DisplayName("Kiểm tra xóa quảng cáo khi ID không tồn tại")
    void testDeleteAdImage_whenIdDoesNotExist() {
        doThrow(new ResourceNotFoundException("Quảng cáo với ID 1 không tồn tại")).when(adImageService).deleteAdImage("1");

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> adImageController.deleteAdImage("1"));
        assertEquals("Quảng cáo với ID 1 không tồn tại", exception.getMessage());
        verify(adImageService, times(1)).deleteAdImage("1");
    }
}