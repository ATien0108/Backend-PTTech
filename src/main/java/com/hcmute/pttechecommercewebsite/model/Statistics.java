package com.hcmute.pttechecommercewebsite.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "Statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statistics {

    @Id
    private ObjectId id;                            // ID duy nhất của thống kê

    private Date date;                              // Ngày thống kê

    private int totalOrders;                        // Tổng số đơn hàng trong ngày
    private int totalItemsSold;                     // Tổng số sản phẩm đã bán
    private double totalRevenue;                    // Tổng doanh thu
    private double totalDiscounts;                  // Tổng số tiền giảm giá
    private double totalPaymentAmount;              // Tổng số tiền thanh toán
    private double totalShippingCosts;              // Tổng chi phí vận chuyển
    private double totalPaymentFees;                // Tổng phí thanh toán

    private List<ProductSales> topSellingProducts;   // Danh sách sản phẩm bán chạy

    private int newCustomers;                       // Số khách hàng mới
    private int totalCustomers;                     // Tổng số khách hàng đã mua hàng
    private int returningCustomers;                 // Số khách hàng quay lại

    private CustomerFeedback customerFeedback;           // Thông tin về đánh giá của khách hàng

    private double averageOrderValue;                     // Giá trị trung bình của đơn hàng
    private double highestOrderValue;                     // Giá trị đơn hàng cao nhất
    private double lowestOrderValue;                      // Giá trị đơn hàng thấp nhất

    private Map<String, Integer> totalOrdersByPaymentMethod;  // Phân loại theo phương thức thanh toán
    private Map<String, Integer> totalOrdersByStatus;         // Phân loại theo trạng thái đơn hàng
    private Map<String, Integer> totalOrdersByShippingMethod; // Phân loại theo phương thức vận chuyển

    private int totalReturnedItems;                         // Tổng số sản phẩm trả lại
    private double totalRevenueFromReturns;                 // Doanh thu từ các đơn hàng trả lại

    @CreatedDate
    private Date createdAt;                                // Thời gian tạo thống kê

    @LastModifiedDate
    private Date updatedAt;                                // Thời gian cập nhật thống kê

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSales {
        private ObjectId productId;         // ID sản phẩm
        private String productName;         // Tên sản phẩm
        private int quantitySold;           // Số lượng bán
        private double revenueFromProduct;  // Doanh thu từ sản phẩm
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerFeedback {
        private double averageRating;    // Đánh giá trung bình
        private int totalReviews;        // Tổng số đánh giá
        private int positiveReviews;     // Số đánh giá tích cực
        private int negativeReviews;     // Số đánh giá tiêu cực
    }
}