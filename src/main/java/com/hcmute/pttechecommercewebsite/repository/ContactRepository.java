package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.Contact;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {

    Optional<Contact> findByIdAndIsDeletedFalse(String id);

    List<Contact> findByIsDeletedFalseAndIsActiveTrue(Sort sort);

    List<Contact> findByIsDeletedFalse(Sort sort);

    // Tìm tất cả liên hệ có thời gian lên lịch nhỏ hơn hoặc bằng thời gian hiện tại và chưa kích hoạt
    List<Contact> findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(Date now);
}