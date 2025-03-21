package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.Inventory;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends MongoRepository<Inventory, String> {
    List<Inventory> findByIsDeletedFalse(Sort sort);

    // Phương thức để lọc nhập kho theo danh sách Product ID
    List<Inventory> findByIsDeletedFalseAndProducts_ProductIdIn(List<ObjectId> productIds);
}