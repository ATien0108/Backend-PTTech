package com.hcmute.pttechecommercewebsite.controller;

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

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

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
    public OrderDTO cancelOrder(@PathVariable String orderId) {
        return orderService.cancelOrder(orderId);
    }

    // API xóa đơn hàng
    @DeleteMapping("/{orderId}")
    public OrderDTO deleteOrder(@PathVariable String orderId) {
        return orderService.deleteOrder(orderId);
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