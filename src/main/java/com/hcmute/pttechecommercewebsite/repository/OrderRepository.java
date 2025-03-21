package com.hcmute.pttechecommercewebsite.repository;

import com.hcmute.pttechecommercewebsite.model.Order;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    // Tìm tất cả đơn hàng không bị xóa
    List<Order> findByIsDeletedFalse();

    // Tìm đơn hàng theo ID và chỉ lấy đơn hàng chưa bị xóa
    Optional<Order> findByIdAndIsDeletedFalse(String id);

    // Tìm đơn hàng theo orderId và chỉ lấy đơn hàng chưa bị xóa
    Optional<Order> findByOrderIdAndIsDeletedFalse(String orderId);

    // Tìm tất cả đơn hàng theo userId và chỉ lấy đơn hàng chưa bị xóa
    List<Order> findByUserIdAndIsDeletedFalse(ObjectId userId);

    // Tìm đơn hàng chứa sản phẩm với productId và chỉ lấy đơn hàng chưa bị xóa
    List<Order> findByItemsProductIdAndIsDeletedFalse(ObjectId productId);

    // Tìm top 10 đơn hàng có totalItems cao nhất và chỉ lấy đơn hàng chưa bị xóa
    List<Order> findTop10ByIsDeletedFalseOrderByTotalItemsDesc();

    // Tìm top 10 đơn hàng có finalPrice cao nhất và chỉ lấy đơn hàng chưa bị xóa
    List<Order> findTop10ByIsDeletedFalseOrderByFinalPriceDesc();

    // Tìm đơn hàng có trạng thái "Chờ xác nhận" và thời gian tạo trước một thời điểm nhất định
    List<Order> findByOrderStatusAndCreatedAtBefore(String orderStatus, Date createdAt);

    // Tìm đơn hàng theo các tiêu chí lọc và chỉ lấy đơn hàng chưa bị xóa
    List<Order> findByPaymentMethodAndIsDeletedFalse(String paymentMethod);
    List<Order> findByPaymentStatusAndIsDeletedFalse(String paymentStatus);
    List<Order> findByOrderStatusAndIsDeletedFalse(String orderStatus);
    List<Order> findByShippingMethodAndIsDeletedFalse(String shippingMethod);
}