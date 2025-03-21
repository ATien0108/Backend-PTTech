package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.ContactDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.model.Contact;
import com.hcmute.pttechecommercewebsite.repository.ContactRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
public class ContactService {

    private final ContactRepository contactRepository;

    @Autowired
    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    // Chuyển từ Entity sang DTO
    private ContactDTO convertToDTO(Contact contact) {
        return ContactDTO.builder()
                .id(contact.getId())
                .companyName(contact.getCompanyName())
                .email(contact.getEmail())
                .phoneNumber(contact.getPhoneNumber())
                .address(ContactDTO.AddressDTO.builder()
                        .street(contact.getAddress().getStreet())
                        .city(contact.getAddress().getCity())
                        .district(contact.getAddress().getDistrict())
                        .country(contact.getAddress().getCountry())
                        .build())
                .socialMedia(ContactDTO.SocialMediaDTO.builder()
                        .facebook(contact.getSocialMedia().getFacebook())
                        .instagram(contact.getSocialMedia().getInstagram())
                        .twitter(contact.getSocialMedia().getTwitter())
                        .zalo(contact.getSocialMedia().getZalo())
                        .build())
                .supportHours(ContactDTO.SupportHoursDTO.builder()
                        .weekdays(contact.getSupportHours().getWeekdays())
                        .weekends(contact.getSupportHours().getWeekends())
                        .build())
                .isActive(contact.isActive())
                .isDeleted(contact.isDeleted())
                .build();
    }

    // Lấy tất cả thông tin liên hệ không bị xóa và đang hiển thị
    public List<ContactDTO> getAllContacts(String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        }

        return contactRepository.findByIsDeletedFalseAndIsActiveTrue(sort)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy tất cả thông tin liên hệ không bị xóa
    public List<ContactDTO> getAllContactsWithDeletedFalse(String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        }

        return contactRepository.findByIsDeletedFalse(sort)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy thông tin liên hệ theo ID
    public Optional<ContactDTO> getContactById(String id) {
        return contactRepository.findByIdAndIsDeletedFalse(id)
                .map(this::convertToDTO);
    }

    // Thêm mới thông tin liên hệ
    public ContactDTO createContact(ContactDTO contactDTO) {
        Contact contact = Contact.builder()
                .companyName(contactDTO.getCompanyName())
                .email(contactDTO.getEmail())
                .phoneNumber(contactDTO.getPhoneNumber())
                .address(new Contact.Address(contactDTO.getAddress().getStreet(), contactDTO.getAddress().getCity(), contactDTO.getAddress().getDistrict(), contactDTO.getAddress().getCountry()))
                .socialMedia(new Contact.SocialMedia(contactDTO.getSocialMedia().getFacebook(), contactDTO.getSocialMedia().getInstagram(), contactDTO.getSocialMedia().getTwitter(), contactDTO.getSocialMedia().getZalo()))
                .supportHours(new Contact.SupportHours(contactDTO.getSupportHours().getWeekdays(), contactDTO.getSupportHours().getWeekends()))
                .isActive(true)
                .isDeleted(false)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        Contact savedContact = contactRepository.save(contact);
        return convertToDTO(savedContact);
    }

    public ContactDTO scheduleCreateContact(ContactDTO contactDTO) {
        // Kiểm tra nếu thời gian lên lịch là trong tương lai
        if (contactDTO.getScheduledDate() != null && contactDTO.getScheduledDate().before(new Date())) {
            throw new IllegalArgumentException("Thời gian phải trong tương lai.");
        }

        // Tạo mới thông tin liên hệ với thời gian lên lịch
        Contact contact = Contact.builder()
                .companyName(contactDTO.getCompanyName())
                .email(contactDTO.getEmail())
                .phoneNumber(contactDTO.getPhoneNumber())
                .address(new Contact.Address(contactDTO.getAddress().getStreet(), contactDTO.getAddress().getCity(), contactDTO.getAddress().getDistrict(), contactDTO.getAddress().getCountry()))
                .socialMedia(new Contact.SocialMedia(contactDTO.getSocialMedia().getFacebook(), contactDTO.getSocialMedia().getInstagram(), contactDTO.getSocialMedia().getTwitter(), contactDTO.getSocialMedia().getZalo()))
                .supportHours(new Contact.SupportHours(contactDTO.getSupportHours().getWeekdays(), contactDTO.getSupportHours().getWeekends()))
                .isActive(false)
                .isDeleted(false)
                .scheduledDate(contactDTO.getScheduledDate())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        Contact savedContact = contactRepository.save(contact);
        return convertToDTO(savedContact);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndActivateScheduledContacts() {
        Date now = new Date();

        List<Contact> scheduledContacts = contactRepository.findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(now);

        for (Contact contact : scheduledContacts) {
            contact.setActive(true);
            contact.setScheduledDate(null);
            contact.setUpdatedAt(now);
            contactRepository.save(contact);
        }
    }

    // Chỉnh sửa thông tin liên hệ
    public ContactDTO updateContact(String id, ContactDTO contactDTO) {
        Optional<Contact> existingContact = contactRepository.findByIdAndIsDeletedFalse(id);
        if (existingContact.isPresent()) {
            Contact contact = existingContact.get();
            contact.setCompanyName(contactDTO.getCompanyName());
            contact.setEmail(contactDTO.getEmail());
            contact.setPhoneNumber(contactDTO.getPhoneNumber());
            contact.setAddress(new Contact.Address(contactDTO.getAddress().getStreet(), contactDTO.getAddress().getCity(), contactDTO.getAddress().getDistrict(), contactDTO.getAddress().getCountry()));
            contact.setSocialMedia(new Contact.SocialMedia(contactDTO.getSocialMedia().getFacebook(), contactDTO.getSocialMedia().getInstagram(), contactDTO.getSocialMedia().getTwitter(), contactDTO.getSocialMedia().getZalo()));
            contact.setSupportHours(new Contact.SupportHours(contactDTO.getSupportHours().getWeekdays(), contactDTO.getSupportHours().getWeekends()));
            contact.setUpdatedAt(new Date());

            Contact updatedContact = contactRepository.save(contact);
            return convertToDTO(updatedContact);
        } else {
            throw new ResourceNotFoundException("Thông tin liên hệ với ID " + id + " không tồn tại hoặc đã bị ẩn");
        }
    }

    // Ẩn liên hệ
    public ContactDTO hideContact(String id) {
        Optional<Contact> optionalContact = contactRepository.findByIdAndIsDeletedFalse(id);
        if (!optionalContact.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin liên hệ với ID: " + id);
        }

        Contact contact = optionalContact.get();
        contact.setActive(false);
        contact.setUpdatedAt(new Date());
        contact = contactRepository.save(contact);
        return convertToDTO(contact);
    }

    // Hiện liên hệ
    public ContactDTO showContact(String id) {
        Optional<Contact> optionalContact = contactRepository.findByIdAndIsDeletedFalse(id);
        if (!optionalContact.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin liên hệ với ID: " + id);
        }

        Contact contact = optionalContact.get();
        contact.setActive(true);
        contact.setUpdatedAt(new Date());
        contact = contactRepository.save(contact);
        return convertToDTO(contact);
    }

    // Xóa thông tin liên hệ (soft delete)
    public void deleteContact(String id) {
        Optional<Contact> contact = contactRepository.findByIdAndIsDeletedFalse(id);
        if (contact.isPresent()) {
            Contact existingContact = contact.get();
            existingContact.setDeleted(true);
            existingContact.setUpdatedAt(new Date());
            contactRepository.save(existingContact);
        } else {
            throw new ResourceNotFoundException("Thông tin liên hệ với ID " + id + " không tồn tại hoặc đã bị xóa");
        }
    }

    // Phương thức xuất tất cả thông tin liên hệ ra file Excel
    public ByteArrayOutputStream exportContactsToExcel(String sortBy, String sortOrder) throws IOException {
        // Lấy tất cả liên hệ từ repository với sắp xếp
        List<ContactDTO> contacts = getAllContactsWithDeletedFalse(sortBy, sortOrder);

        // Tạo workbook Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Contacts");

        // Định dạng chung cho workbook
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Tạo dòng tiêu đề
        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Tên công ty", "Email", "Số điện thoại", "Địa chỉ", "Facebook", "Instagram", "Twitter", "Zalo", "Giờ hỗ trợ (Ngày trong tuần)", "Giờ hỗ trợ (Cuối tuần)", "Trạng thái hiển thị", "Trạng thái xóa"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Định dạng cho dữ liệu
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Thêm dữ liệu liên hệ vào file Excel
        int rowNum = 1;
        for (ContactDTO contact : contacts) {
            Row row = sheet.createRow(rowNum++);

            // Cột ID
            row.createCell(0).setCellValue(contact.getId());
            row.getCell(0).setCellStyle(dataStyle);

            // Cột Tên công ty
            row.createCell(1).setCellValue(contact.getCompanyName());
            row.getCell(1).setCellStyle(dataStyle);

            // Cột Email
            row.createCell(2).setCellValue(contact.getEmail());
            row.getCell(2).setCellStyle(dataStyle);

            // Cột Số điện thoại
            row.createCell(3).setCellValue(contact.getPhoneNumber());
            row.getCell(3).setCellStyle(dataStyle);

            // Cột Địa chỉ
            row.createCell(4).setCellValue(contact.getAddress().getStreet() + ", " + contact.getAddress().getCity() + ", " + contact.getAddress().getDistrict() + ", " + contact.getAddress().getCountry());
            row.getCell(4).setCellStyle(dataStyle);

            // Cột Facebook
            row.createCell(5).setCellValue(contact.getSocialMedia().getFacebook() != null ? contact.getSocialMedia().getFacebook() : "Không có");
            row.getCell(5).setCellStyle(dataStyle);

            // Cột Instagram
            row.createCell(6).setCellValue(contact.getSocialMedia().getInstagram() != null ? contact.getSocialMedia().getInstagram() : "Không có");
            row.getCell(6).setCellStyle(dataStyle);

            // Cột Twitter
            row.createCell(7).setCellValue(contact.getSocialMedia().getTwitter() != null ? contact.getSocialMedia().getTwitter() : "Không có");
            row.getCell(7).setCellStyle(dataStyle);

            // Cột Zalo
            row.createCell(8).setCellValue(contact.getSocialMedia().getZalo() != null ? contact.getSocialMedia().getZalo() : "Không có");
            row.getCell(8).setCellStyle(dataStyle);

            // Cột Giờ hỗ trợ (Ngày trong tuần)
            row.createCell(9).setCellValue(contact.getSupportHours().getWeekdays());
            row.getCell(9).setCellStyle(dataStyle);

            // Cột Giờ hỗ trợ (Cuối tuần)
            row.createCell(10).setCellValue(contact.getSupportHours().getWeekends());
            row.getCell(10).setCellStyle(dataStyle);

            // Cột Trạng thái hiển thị
            row.createCell(11).setCellValue(contact.isActive() ? "Đang hiển thị" : "Ẩn");
            row.getCell(11).setCellStyle(dataStyle);

            // Cột Trạng thái xóa
            row.createCell(12).setCellValue(contact.isDeleted() ? "Đã xóa" : "Đang hiển thị");
            row.getCell(12).setCellStyle(dataStyle);
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