package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.Category;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    // Tìm danh mục theo ID
    Optional<Category> findById(String id);

    // Tìm tất cả danh mục mà không bị xóa (isDeleted == false) và hiển thị (isVisible == true)
    List<Category> findByIsDeletedFalseAndIsActiveTrue(Sort sort);

    // Tìm tất cả danh mục mà không bị xóa (isDeleted == false)
    List<Category> findByIsDeletedFalse(Sort sort);

    Optional<Category> findByIdAndIsDeletedFalse(String id);

    // Tìm tất cả danh mục con thuộc danh mục cha mà không bị xóa (isDeleted == false) và hiển thị (isVisible == true)
    List<Category> findByParentCategoryIdAndIsDeletedFalseAndIsActiveTrue(ObjectId parentCategoryId);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isDeleted': false, 'isVisible': true }")
    List<Category> findByNameContaining(String keyword);

    // Tìm tất cả danh mục có thời gian lên lịch nhỏ hơn hoặc bằng thời gian hiện tại và chưa kích hoạt
    List<Category> findByScheduledDateBeforeAndIsDeletedFalseAndIsActiveFalse(Date now);

    List<Category> findByNameInIgnoreCase(List<String> names);
}