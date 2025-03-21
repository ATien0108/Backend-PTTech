package com.hcmute.pttechecommercewebsite.controller;

import com.hcmute.pttechecommercewebsite.dto.ContactDTO;
import com.hcmute.pttechecommercewebsite.exception.MessageResponse;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    // Xem tất cả thông tin liên hệ
    @GetMapping
    public ResponseEntity<List<ContactDTO>> getAllContacts(
            @RequestParam(value = "sortBy", defaultValue = "companyName") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {

        List<ContactDTO> contacts = contactService.getAllContacts(sortBy, sortOrder);
        if (contacts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    // Xem tất cả thông tin liên hệ
    @GetMapping("/no-delete")
    public ResponseEntity<List<ContactDTO>> getAllContactsWithDeletedFalse(
            @RequestParam(value = "sortBy", defaultValue = "companyName") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {

        List<ContactDTO> contacts = contactService.getAllContactsWithDeletedFalse(sortBy, sortOrder);
        if (contacts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    // Xem thông tin liên hệ theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> getContactById(@PathVariable String id) {
        return contactService.getContactById(id)
                .map(contact -> new ResponseEntity<>(contact, HttpStatus.OK))
                .orElseThrow(() -> new ResourceNotFoundException("Thông tin liên hệ với ID " + id + " không tồn tại hoặc đã bị ẩn"));
    }

    // Thêm mới thông tin liên hệ
    @PostMapping
    public ResponseEntity<ContactDTO> createContact(@ModelAttribute ContactDTO contactDTO) {
        ContactDTO createdContact = contactService.createContact(contactDTO);
        return new ResponseEntity<>(createdContact, HttpStatus.CREATED);
    }

    // API tạo liên hệ với thời gian lên lịch
    @PostMapping("/schedule-create")
    public ResponseEntity<ContactDTO> scheduleCreateContact(@RequestBody ContactDTO contactDTO) {
        ContactDTO scheduledContact = contactService.scheduleCreateContact(contactDTO);
        return new ResponseEntity<>(scheduledContact, HttpStatus.CREATED);
    }

    // Chỉnh sửa thông tin liên hệ
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateContact(@PathVariable String id, @ModelAttribute ContactDTO contactDTO) {
        ContactDTO updatedContact = contactService.updateContact(id, contactDTO);
        return new ResponseEntity<>(new MessageResponse("Chỉnh sửa thông tin liên hệ thành công!", updatedContact), HttpStatus.OK);
    }

    // Ẩn thông tin liên hệ
    @PutMapping("/hide/{id}")
    public ResponseEntity<Object> hideContact(@PathVariable String id) {
        ContactDTO hiddenContact = contactService.hideContact(id);
        return new ResponseEntity<>(new MessageResponse("Thông tin liên hệ đã được ẩn thành công", hiddenContact), HttpStatus.OK);
    }

    // Hiện thông tin liên hệ
    @PutMapping("/show/{id}")
    public ResponseEntity<Object> showContact(@PathVariable String id) {
        try {
            ContactDTO contactDTO = contactService.showContact(id);
            return new ResponseEntity<>(contactDTO, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Không tìm thấy thông tin liên hệ với ID: " + id, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi khi hiện thông tin liên hệ", e.getMessage()));
        }
    }

    // Xóa thông tin liên hệ (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteContact(@PathVariable String id) {
        contactService.deleteContact(id);
        return new ResponseEntity<>(new MessageResponse("Bạn đã thực hiện xóa thành công thông tin liên hệ với ID: " + id, id), HttpStatus.OK);
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportContactsToExcel(
            @RequestParam(value = "sortBy", defaultValue = "companyName") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {

        try {
            ByteArrayOutputStream outputStream = contactService.exportContactsToExcel(sortBy, sortOrder);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Lỗi khi xuất file Excel: " + e.getMessage()).getBytes());
        }
    }
}