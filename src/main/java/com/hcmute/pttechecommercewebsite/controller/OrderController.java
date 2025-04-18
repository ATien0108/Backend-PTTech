package com.hcmute.pttechecommercewebsite.controller;

import com.hcmute.pttechecommercewebsite.config.VNPayConfig;
import com.hcmute.pttechecommercewebsite.util.VNPayUtil;
import com.hcmute.pttechecommercewebsite.dto.OrderDTO;
import com.hcmute.pttechecommercewebsite.service.OrderService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private VNPayConfig vnPayConfig;

    // Lấy tất cả đơn hàng (có thể lọc theo các điều kiện)
    @GetMapping
    public List<OrderDTO> getAllOrders(@RequestParam(required = false) String paymentMethod,
                                       @RequestParam(required = false) String paymentStatus,
                                       @RequestParam(required = false) String orderStatus,
                                       @RequestParam(required = false) String shippingMethod,
                                       @RequestParam(required = false, defaultValue = "latest") String sortBy) {
        return orderService.getAllOrders(paymentMethod, paymentStatus, orderStatus, shippingMethod, sortBy);
    }

    // Lấy đơn hàng theo ID
    @GetMapping("/{id}")
    public OrderDTO getOrderById(@PathVariable String id) {
        return orderService.getOrderById(id);
    }

    // Lấy đơn hàng theo orderId
    @GetMapping("/order-id/{orderId}")
    public OrderDTO getOrderByOrderId(@PathVariable String orderId) {
        return orderService.getOrderByOrderId(orderId);
    }

    // Lấy Top 10 đơn hàng có totalItems cao nhất
    @GetMapping("/top-10-items")
    public List<OrderDTO> getTop10OrdersByTotalItems() {
        return orderService.getTop10OrdersByTotalItems();
    }

    // Lấy Top 10 đơn hàng có finalPrice cao nhất
    @GetMapping("/top-10-price")
    public List<OrderDTO> getTop10OrdersByFinalPrice() {
        return orderService.getTop10OrdersByFinalPrice();
    }

    // Lấy tất cả đơn hàng theo userId
    @GetMapping("/user/{userId}")
    public List<OrderDTO> getOrdersByUserId(@PathVariable String userId) {
        return orderService.getOrdersByUserId(new ObjectId(userId));
    }

    // Lấy tất cả đơn hàng chứa productId
    @GetMapping("/product/{productId}")
    public List<OrderDTO> getOrdersByProductId(@PathVariable String productId) {
        return orderService.getOrdersByProductId(new ObjectId(productId));
    }

    // Tạo đơn hàng mới
    @PostMapping
    public OrderDTO createOrder(@RequestBody OrderDTO orderDTO) {
        return orderService.createOrder(orderDTO);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable String orderId, @RequestBody OrderDTO updatedOrderDTO) {
        try {
            OrderDTO updatedOrder = orderService.updateOrder(orderId, updatedOrderDTO);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // API hủy đơn hàng
    @PostMapping("/cancel/{orderId}")
    public OrderDTO cancelOrder(@PathVariable String orderId, @RequestParam String cancellationReason) {
        return orderService.cancelOrder(orderId, cancellationReason);
    }

    // API gửi yêu cầu trả hàng
    @PostMapping("/{orderId}/request-return")
    public ResponseEntity<OrderDTO> requestReturn(@PathVariable String orderId, @RequestParam String returnReason) {
        try {
            OrderDTO updatedOrder = orderService.requestReturn(orderId, returnReason);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // API hoàn tất trả hàng
    @PostMapping("/{orderId}/complete-return")
    public ResponseEntity<OrderDTO> completeReturn(@PathVariable String orderId) {
        try {
            OrderDTO updatedOrder = orderService.completeReturn(orderId);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // API từ chối yêu cầu trả hàng
    @PostMapping("/{orderId}/reject-return")
    public ResponseEntity<OrderDTO> rejectReturn(
            @PathVariable String orderId,
            @RequestParam String rejectionReason) {
        try {
            OrderDTO updatedOrder = orderService.rejectReturn(orderId, rejectionReason);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // API xóa đơn hàng
    @DeleteMapping("/{orderId}")
    public OrderDTO deleteOrder(@PathVariable String orderId) {
        return orderService.deleteOrder(orderId);
    }

    @PostMapping("/vnpay/{orderId}")
    public ResponseEntity<String> initiateVNPayPayment(@PathVariable String orderId) {
        try {
            OrderDTO orderDTO = orderService.getOrderById(orderId);
            if (orderDTO == null) {
                return ResponseEntity.badRequest().body("Đơn hàng không tồn tại");
            }

            double amount = orderDTO.getFinalPrice();
            String paymentUrl = VNPayUtil.getPaymentUrl(orderId, amount, vnPayConfig);

            return ResponseEntity.ok(paymentUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi tạo URL thanh toán VNPay: " + e.getMessage());
        }
    }

    @PostMapping("/vnpay/return")
    public ResponseEntity<String> handleVNPayReturn(
            @RequestParam String vnp_ResponseCode,
            @RequestParam String vnp_TransactionStatus,
            @RequestParam String vnp_TxnRef) {

        // Giao dịch thành công
        if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
            orderService.updateOrderPaymentStatus(vnp_TxnRef, "Đã thanh toán");
            return ResponseEntity.ok("Giao dịch thành công: " + vnp_TxnRef);
        }

        // Giao dịch bị nghi ngờ
        if ("07".equals(vnp_ResponseCode)) {
            orderService.updateOrderPaymentStatus(vnp_TxnRef, "Nghi ngờ gian lận");
            return ResponseEntity.status(HttpStatus.OK).body("Giao dịch nghi ngờ. Đang chờ kiểm tra: " + vnp_TxnRef);
        }

        // Giao dịch bị hủy
        if ("24".equals(vnp_ResponseCode)) {
            orderService.updateOrderPaymentStatus(vnp_TxnRef, "Khách hàng hủy giao dịch");
            return ResponseEntity.status(HttpStatus.OK).body("Khách hàng đã hủy giao dịch: " + vnp_TxnRef);
        }

        // Trường hợp khác
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Giao dịch thất bại: Mã lỗi " + vnp_ResponseCode);
    }


    // API xuất danh sách đơn hàng ra file Excel
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportOrdersToExcel(@RequestParam(required = false) String paymentMethod,
                                                      @RequestParam(required = false) String paymentStatus,
                                                      @RequestParam(required = false) String orderStatus,
                                                      @RequestParam(required = false) String shippingMethod,
                                                      @RequestParam(required = false, defaultValue = "latest") String sortBy) {
        try {
            // Gọi service để lấy danh sách đơn hàng và xuất ra file Excel
            ByteArrayOutputStream outputStream = orderService.exportOrdersToExcel(paymentMethod, paymentStatus, orderStatus, shippingMethod, sortBy);

            // Thiết lập các header HTTP cho response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // Trả về response chứa dữ liệu file Excel
            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Lỗi khi xuất file Excel: " + e.getMessage()).getBytes());
        }
    }
}