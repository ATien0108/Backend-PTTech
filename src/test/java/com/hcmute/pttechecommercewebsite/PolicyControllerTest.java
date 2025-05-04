package com.hcmute.pttechecommercewebsite;

import com.hcmute.pttechecommercewebsite.controller.PolicyController;
import com.hcmute.pttechecommercewebsite.dto.PolicyDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
public class PolicyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PolicyService policyService;

    @InjectMocks
    private PolicyController policyController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(policyController).build();
    }

    @Test
    @DisplayName("Lấy tất cả chính sách thành công với sắp xếp")
    void getAllPoliciesSuccess() throws Exception {
        // Giả lập danh sách PolicyDTO
        List<PolicyDTO> mockPolicies = List.of(
                new PolicyDTO("1", "Return", "Chính sách hoàn trả", "Mô tả chính sách hoàn trả", "Chi tiết chính sách", true, false, null, null, null),
                new PolicyDTO("2", "Warranty", "Chính sách bảo hành", "Mô tả chính sách bảo hành", "Chi tiết chính sách", true, false, null, null, null)
        );

        // Giả lập hành động của service khi gọi getAllPolicies với tham số sắp xếp
        when(policyService.getAllPolicies("title", "asc")).thenReturn(mockPolicies);

        // Thực hiện yêu cầu GET với các tham số sắp xếp
        mockMvc.perform(get("/api/policies")
                        .param("sortBy", "title")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Chính sách hoàn trả")))
                .andExpect(jsonPath("$[1].title", is("Chính sách bảo hành")));

        // Kiểm tra rằng service được gọi đúng 1 lần với tham số sắp xếp đúng
        verify(policyService, times(1)).getAllPolicies("title", "asc");
    }

    @Test
    @DisplayName("Lấy tất cả chính sách khi không có dữ liệu")
    void getAllPoliciesNoContent() throws Exception {
        // Giả lập rằng không có chính sách nào trong cơ sở dữ liệu
        when(policyService.getAllPolicies("title", "asc")).thenReturn(List.of());

        // Thực hiện yêu cầu GET với các tham số sắp xếp
        mockMvc.perform(get("/api/policies")
                        .param("sortBy", "title")
                        .param("sortOrder", "asc"))
                .andExpect(status().isNoContent());

        // Kiểm tra rằng service được gọi đúng 1 lần với tham số sắp xếp đúng
        verify(policyService, times(1)).getAllPolicies("title", "asc");
    }

    @Test
    @DisplayName("Lấy chính sách theo ID thành công")
    void getPolicyByIdSuccess() throws Exception {
        PolicyDTO mockPolicy = new PolicyDTO("1", "Return", "Chính sách hoàn trả", "Mô tả chính sách hoàn trả", "Chi tiết chính sách", true, false, null, null, null);

        when(policyService.getPolicyById("1")).thenReturn(Optional.of(mockPolicy));

        mockMvc.perform(get("/api/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Chính sách hoàn trả")))
                .andExpect(jsonPath("$.description", is("Mô tả chính sách hoàn trả")));

        verify(policyService, times(1)).getPolicyById("1");
    }

    @Test
    @DisplayName("Lấy chính sách theo ID không tồn tại")
    void getPolicyByIdNotFound() throws Exception {
        when(policyService.getPolicyById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/policies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Chính sách với ID 99 không tồn tại"));

        verify(policyService, times(1)).getPolicyById("99");
    }

    @Test
    @DisplayName("Tìm kiếm chính sách theo tiêu đề thành công")
    void searchPoliciesSuccess() throws Exception {
        List<PolicyDTO> mockPolicies = List.of(
                new PolicyDTO("1", "Return", "Chính sách hoàn trả", "Mô tả chính sách hoàn trả", "Chi tiết chính sách", true, false, null, null, null),
                new PolicyDTO("2", "Warranty", "Chính sách bảo hành", "Mô tả chính sách bảo hành", "Chi tiết chính sách", true, false, null, null, null)
        );

        when(policyService.searchPoliciesByTitle("Chính sách")).thenReturn(mockPolicies);

        mockMvc.perform(get("/api/policies/search").param("keyword", "Chính sách"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Chính sách hoàn trả")))
                .andExpect(jsonPath("$[1].title", is("Chính sách bảo hành")));

        verify(policyService, times(1)).searchPoliciesByTitle("Chính sách");
    }

    @Test
    @DisplayName("Tìm kiếm chính sách theo tiêu đề không có kết quả")
    void searchPoliciesNoContent() throws Exception {
        when(policyService.searchPoliciesByTitle("Không có"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/policies/search").param("keyword", "Không có"))
                .andExpect(status().isNoContent());

        verify(policyService, times(1)).searchPoliciesByTitle("Không có");
    }

    @Test
    @DisplayName("Thêm mới chính sách thành công")
    void createPolicySuccess() throws Exception {
        PolicyDTO newPolicy = new PolicyDTO("3", "Privacy", "Chính sách bảo mật", "Mô tả chính sách bảo mật", "Chi tiết chính sách", true, false, null, null, null);

        when(policyService.createPolicy(any(PolicyDTO.class))).thenReturn(newPolicy);

        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"Privacy\",\"title\":\"Chính sách bảo mật\",\"description\":\"Mô tả chính sách bảo mật\",\"content\":\"Chi tiết chính sách\"}")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Chính sách bảo mật")))
                .andExpect(jsonPath("$.description", is("Mô tả chính sách bảo mật")))
                .andExpect(jsonPath("$.content", is("Chi tiết chính sách")));

        verify(policyService, times(1)).createPolicy(any(PolicyDTO.class));
    }

    @Test
    @DisplayName("Thêm mới chính sách với dữ liệu không hợp lệ")
    void createPolicyBadRequest() throws Exception {
        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"Privacy\",\"title\":\"\",\"description\":\"Mô tả chính sách bảo mật\",\"content\":\"Chi tiết chính sách\"}")
                )
                .andExpect(status().isBadRequest());

        verify(policyService, times(0)).createPolicy(any(PolicyDTO.class));
    }

    @Test
    @DisplayName("Chỉnh sửa chính sách thành công")
    void updatePolicySuccess() throws Exception {
        PolicyDTO updatedPolicy = new PolicyDTO("1", "Return", "Chính sách hoàn trả", "Mô tả đã sửa", "Chi tiết chính sách đã sửa", true, false, null, null, null);

        when(policyService.updatePolicy(eq("1"), any(PolicyDTO.class))).thenReturn(updatedPolicy);

        mockMvc.perform(put("/api/policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"Return\",\"title\":\"Chính sách hoàn trả\",\"description\":\"Mô tả đã sửa\",\"content\":\"Chi tiết chính sách đã sửa\",\"isVisible\":true}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Chỉnh sửa chính sách thành công!")))
                .andExpect(jsonPath("$.data.id", is("1")))
                .andExpect(jsonPath("$.data.type", is("Return")))
                .andExpect(jsonPath("$.data.title", is("Chính sách hoàn trả")));

        verify(policyService, times(1)).updatePolicy(eq("1"), any(PolicyDTO.class));
    }

    @Test
    @DisplayName("Chỉnh sửa chính sách không tồn tại")
    void updatePolicyNotFound() throws Exception {
        when(policyService.updatePolicy(eq("99"), any(PolicyDTO.class))).thenThrow(new ResourceNotFoundException("Chính sách với ID 99 không tồn tại"));

        mockMvc.perform(put("/api/policies/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"Return\",\"title\":\"Chính sách hoàn trả\",\"description\":\"Mô tả đã sửa\",\"content\":\"Chi tiết chính sách đã sửa\",\"isVisible\":true}")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Chính sách với ID 99 không tồn tại")));

        verify(policyService, times(1)).updatePolicy(eq("99"), any(PolicyDTO.class));
    }

    @Test
    @DisplayName("Xóa chính sách thành công")
    void deletePolicySuccess() throws Exception {
        doNothing().when(policyService).deletePolicy("1");

        mockMvc.perform(delete("/api/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Bạn đã thực hiện xóa thành công chính sách với ID: 1")));

        verify(policyService, times(1)).deletePolicy("1");
    }

    @Test
    @DisplayName("Xóa chính sách không tồn tại")
    void deletePolicyNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Chính sách với ID 99 không tồn tại")).when(policyService).deletePolicy("99");

        mockMvc.perform(delete("/api/policies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Chính sách với ID 99 không tồn tại hoặc đã bị xóa")));

        verify(policyService, times(1)).deletePolicy("99");
    }
}
