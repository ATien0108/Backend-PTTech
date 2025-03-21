package com.hcmute.pttechecommercewebsite;

import com.hcmute.pttechecommercewebsite.controller.ContactController;
import com.hcmute.pttechecommercewebsite.dto.ContactDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.service.ContactService;
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
public class ContactControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContactService contactService;

    @InjectMocks
    private ContactController contactController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(contactController).build();
    }

    @Test
    @DisplayName("Lấy tất cả thông tin liên hệ thành công với sắp xếp")
    void getAllContactsSuccess() throws Exception {
        List<ContactDTO> mockContacts = List.of(
                new ContactDTO("1", "TechCorp", "support@techcorp.com", "+123456789",
                        new ContactDTO.AddressDTO("123 Tech Street", "Tech City", "Tech District", "USA"),
                        new ContactDTO.SocialMediaDTO("https://facebook.com/techcorp", "https://instagram.com/techcorp",
                                "https://twitter.com/techcorp", "https://zalo.me/techcorp"),
                        new ContactDTO.SupportHoursDTO("8:00 AM - 6:00 PM", "9:00 AM - 3:00 PM"),
                        true, false, null),
                new ContactDTO("2", "GadgetHub", "contact@gadgethub.com", "+987654321",
                        new ContactDTO.AddressDTO("456 Innovation Road", "New York", "Tech Valley", "USA"),
                        new ContactDTO.SocialMediaDTO("https://facebook.com/gadgethub", "https://instagram.com/gadgethub",
                                "https://twitter.com/gadgethub", null),
                        new ContactDTO.SupportHoursDTO("9:00 AM - 7:00 PM", "10:00 AM - 4:00 PM"),
                        true, false, null)
        );

        when(contactService.getAllContacts("companyName", "desc")).thenReturn(mockContacts);

        mockMvc.perform(get("/api/contacts")
                        .param("sortBy", "companyName")   // Sắp xếp theo tên công ty
                        .param("sortOrder", "desc"))      // Theo thứ tự giảm dần
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].companyName", is("TechCorp")))
                .andExpect(jsonPath("$[1].companyName", is("GadgetHub")));

        verify(contactService, times(1)).getAllContacts("companyName", "desc");
    }

    @Test
    @DisplayName("Lấy tất cả thông tin liên hệ khi không có dữ liệu")
    void getAllContactsNoContent() throws Exception {
        when(contactService.getAllContacts("companyName", "desc")).thenReturn(List.of());

        mockMvc.perform(get("/api/contacts")
                        .param("sortBy", "companyName")   // Sắp xếp theo tên công ty
                        .param("sortOrder", "desc"))      // Theo thứ tự giảm dần
                .andExpect(status().isNoContent());

        verify(contactService, times(1)).getAllContacts("companyName", "desc");
    }

    @Test
    @DisplayName("Lấy thông tin liên hệ theo ID thành công")
    void getContactByIdSuccess() throws Exception {
        ContactDTO mockContact = new ContactDTO("1", "TechCorp", "support@techcorp.com", "+123456789",
                new ContactDTO.AddressDTO("123 Tech Street", "Tech City", "Tech District", "USA"),
                new ContactDTO.SocialMediaDTO("https://facebook.com/techcorp", "https://instagram.com/techcorp",
                        "https://twitter.com/techcorp", "https://zalo.me/techcorp"),
                new ContactDTO.SupportHoursDTO("8:00 AM - 6:00 PM", "9:00 AM - 3:00 PM"),
                true, false, null);

        when(contactService.getContactById("1")).thenReturn(Optional.of(mockContact));

        mockMvc.perform(get("/api/contacts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName", is("TechCorp")))
                .andExpect(jsonPath("$.email", is("support@techcorp.com")));

        verify(contactService, times(1)).getContactById("1");
    }

    @Test
    @DisplayName("Lấy thông tin liên hệ theo ID không tồn tại")
    void getContactByIdNotFound() throws Exception {
        when(contactService.getContactById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/contacts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Thông tin liên hệ với ID 99 không tồn tại hoặc đã bị ẩn"));

        verify(contactService, times(1)).getContactById("99");
    }

    @Test
    @DisplayName("Thêm mới thông tin liên hệ thành công")
    void createContactSuccess() throws Exception {
        ContactDTO contactDTO = new ContactDTO("3", "NewTech", "contact@newtech.com", "+1122334455",
                new ContactDTO.AddressDTO("789 Innovation Lane", "TechCity", "TechDistrict", "USA"),
                new ContactDTO.SocialMediaDTO("https://facebook.com/newtech", "https://instagram.com/newtech",
                        "https://twitter.com/newtech", "https://zalo.me/newtech"),
                new ContactDTO.SupportHoursDTO("9:00 AM - 5:00 PM", "Closed"),
                true, false, null);

        when(contactService.createContact(any(ContactDTO.class))).thenReturn(contactDTO);

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyName\":\"NewTech\",\"email\":\"contact@newtech.com\",\"phoneNumber\":\"+1122334455\","
                                + "\"address\":{\"street\":\"789 Innovation Lane\",\"city\":\"TechCity\",\"district\":\"TechDistrict\",\"country\":\"USA\"},"
                                + "\"socialMedia\":{\"facebook\":\"https://facebook.com/newtech\",\"instagram\":\"https://instagram.com/newtech\","
                                + "\"twitter\":\"https://twitter.com/newtech\",\"zalo\":\"https://zalo.me/newtech\"},"
                                + "\"supportHours\":{\"weekdays\":\"9:00 AM - 5:00 PM\",\"weekends\":\"Closed\"},\"isActive\":true,\"isDeleted\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName", is("NewTech")))
                .andExpect(jsonPath("$.email", is("contact@newtech.com")))
                .andExpect(jsonPath("$.phoneNumber", is("+1122334455")));

        verify(contactService, times(1)).createContact(any(ContactDTO.class));
    }

    @Test
    @DisplayName("Thêm mới thông tin liên hệ không hợp lệ")
    void createContactInvalidData() throws Exception {
        ContactDTO invalidContactDTO = new ContactDTO(null, "InvalidCompany", "invalidemail", "+0000",
                new ContactDTO.AddressDTO("", "", "", ""),
                new ContactDTO.SocialMediaDTO("", "", "", ""),
                new ContactDTO.SupportHoursDTO("", ""),
                false, true, null);

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyName\":\"InvalidCompany\",\"email\":\"invalidemail\",\"phoneNumber\":\"+0000\","
                                + "\"address\":{\"street\":\"\",\"city\":\"\",\"district\":\"\",\"country\":\"\"},"
                                + "\"socialMedia\":{\"facebook\":\"\",\"instagram\":\"\",\"twitter\":\"\",\"zalo\":\"\"},"
                                + "\"supportHours\":{\"weekdays\":\"\",\"weekends\":\"\"},\"isActive\":false,\"isDeleted\":true}"))
                .andExpect(status().isBadRequest()); // Đây sẽ trả về lỗi 400 nếu dữ liệu không hợp lệ

        verify(contactService, times(0)).createContact(any(ContactDTO.class));
    }

    @Test
    @DisplayName("Chỉnh sửa thông tin liên hệ thành công")
    void updateContactSuccess() throws Exception {
        ContactDTO existingContactDTO = new ContactDTO("1", "UpdatedTech", "updated@techcorp.com", "+123456789",
                new ContactDTO.AddressDTO("123 Tech Street", "Tech City", "Tech District", "USA"),
                new ContactDTO.SocialMediaDTO("https://facebook.com/updatedtech", "https://instagram.com/updatedtech",
                        "https://twitter.com/updatedtech", "https://zalo.me/updatedtech"),
                new ContactDTO.SupportHoursDTO("8:00 AM - 6:00 PM", "9:00 AM - 3:00 PM"),
                true, false, null);

        when(contactService.updateContact(eq("1"), any(ContactDTO.class))).thenReturn(existingContactDTO);

        mockMvc.perform(put("/api/contacts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyName\":\"UpdatedTech\",\"email\":\"updated@techcorp.com\",\"phoneNumber\":\"+123456789\","
                                + "\"address\":{\"street\":\"123 Tech Street\",\"city\":\"Tech City\",\"district\":\"Tech District\",\"country\":\"USA\"},"
                                + "\"socialMedia\":{\"facebook\":\"https://facebook.com/updatedtech\",\"instagram\":\"https://instagram.com/updatedtech\","
                                + "\"twitter\":\"https://twitter.com/updatedtech\",\"zalo\":\"https://zalo.me/updatedtech\"},"
                                + "\"supportHours\":{\"weekdays\":\"8:00 AM - 6:00 PM\",\"weekends\":\"9:00 AM - 3:00 PM\"},\"isActive\":true,\"isDeleted\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName", is("UpdatedTech")))
                .andExpect(jsonPath("$.email", is("updated@techcorp.com")));

        verify(contactService, times(1)).updateContact(eq("1"), any(ContactDTO.class));
    }

    @Test
    @DisplayName("Chỉnh sửa thông tin liên hệ không tồn tại")
    void updateContactNotFound() throws Exception {
        when(contactService.updateContact(eq("99"), any(ContactDTO.class)))
                .thenThrow(new ResourceNotFoundException("Thông tin liên hệ với ID 99 không tồn tại hoặc đã bị ẩn"));

        mockMvc.perform(put("/api/contacts/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyName\":\"NonExistentTech\",\"email\":\"nonexistent@techcorp.com\",\"phoneNumber\":\"+999999999\","
                                + "\"address\":{\"street\":\"Nonexistent Street\",\"city\":\"Nowhere\",\"district\":\"Nowhere\",\"country\":\"Nowhere\"},"
                                + "\"socialMedia\":{\"facebook\":\"https://facebook.com/nonexistent\",\"instagram\":\"https://instagram.com/nonexistent\","
                                + "\"twitter\":\"https://twitter.com/nonexistent\",\"zalo\":\"https://zalo.me/nonexistent\"},"
                                + "\"supportHours\":{\"weekdays\":\"Closed\",\"weekends\":\"Closed\"},\"isActive\":false,\"isDeleted\":true}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Thông tin liên hệ với ID 99 không tồn tại hoặc đã bị ẩn")));

        verify(contactService, times(1)).updateContact(eq("99"), any(ContactDTO.class));
    }

    @Test
    @DisplayName("Xóa thông tin liên hệ thành công")
    void deleteContactSuccess() throws Exception {
        doNothing().when(contactService).deleteContact("1");

        mockMvc.perform(delete("/api/contacts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Bạn đã thực hiện xóa thành công thông tin liên hệ với ID: 1")));

        verify(contactService, times(1)).deleteContact("1");
    }

    @Test
    @DisplayName("Xóa thông tin liên hệ không tồn tại")
    void deleteContactNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Thông tin liên hệ với ID 99 không tồn tại hoặc đã bị xóa"))
                .when(contactService).deleteContact("99");

        mockMvc.perform(delete("/api/contacts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Thông tin liên hệ với ID 99 không tồn tại hoặc đã bị xóa")));

        verify(contactService, times(1)).deleteContact("99");
    }
}