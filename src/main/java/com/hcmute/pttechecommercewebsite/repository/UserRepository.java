package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Tìm kiếm tất cả user không bị xóa
    List<User> findAllByIsDeletedFalse();

    // Tìm kiếm user theo tên (username)
    Optional<User> findByUsername(String username);

    // Tìm kiếm user theo email
    Optional<User> findByEmail(String email);

    // Tìm người dùng theo verificationToken
    Optional<User> findByVerificationToken(String verificationToken);
}
