package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.OrderDTO;
import com.hcmute.pttechecommercewebsite.model.DiscountCode;
import com.hcmute.pttechecommercewebsite.model.Order;
import com.hcmute.pttechecommercewebsite.model.Product;
import com.hcmute.pttechecommercewebsite.repository.DiscountCodeRepository;
import com.hcmute.pttechecommercewebsite.repository.OrderRepository;
import com.hcmute.pttechecommercewebsite.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    @Autowired
    private ProductRepository productRepository;

    // Lấy tất cả đơn hàng (có thể lọc theo các điều kiện)
    public List<OrderDTO> getAllOrders(String paymentMethod, String paymentStatus, String orderStatus, String shippingMethod, String sortBy) {
        List<Order> orders;

        // Xử lý lọc theo các tiêu chí
        if (paymentMethod != null) {
            orders = orderRepository.findByPaymentMethodAndIsDeletedFalse(paymentMethod);
        } else if (paymentStatus != null) {
            orders = orderRepository.findByPaymentStatusAndIsDeletedFalse(paymentStatus);
        } else if (orderStatus != null) {
            orders = orderRepository.findByOrderStatusAndIsDeletedFalse(orderStatus);
        } else if (shippingMethod != null) {
            orders = orderRepository.findByShippingMethodAndIsDeletedFalse(shippingMethod);
        } else {
            orders = orderRepository.findByIsDeletedFalse();
        }

        // Sort by (nếu có)
        if ("latest".equals(sortBy)) {
            orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        } else if ("oldest".equals(sortBy)) {
            orders.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
        }

        // Chuyển từ model sang DTO
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Lấy đơn hàng theo ID
    public OrderDTO getOrderById(String id) {
        return orderRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    // Lấy đơn hàng theo order_id
    public OrderDTO getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    // Lấy Top 10 đơn hàng có totalItems cao nhất
    public List<OrderDTO> getTop10OrdersByTotalItems() {
        List<Order> orders = orderRepository.findTop10ByIsDeletedFalseOrderByTotalItemsDesc();
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Lấy Top 10 đơn hàng có finalPrice cao nhất
    public List<OrderDTO> getTop10OrdersByFinalPrice() {
        List<Order> orders = orderRepository.findTop10ByIsDeletedFalseOrderByFinalPriceDesc();
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Lấy tất cả đơn hàng của một userId
    public List<OrderDTO> getOrdersByUserId(ObjectId userId) {
        List<Order> orders = orderRepository.findByUserIdAndIsDeletedFalse(userId);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Lấy tất cả đơn hàng chứa một productId
    public List<OrderDTO> getOrdersByProductId(ObjectId productId) {
        List<Order> orders = orderRepository.findByItemsProductIdAndIsDeletedFalse(productId);
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public OrderDTO createOrder(OrderDTO orderDTO) {
        // Tính toán giá trị tổng quan của đơn hàng
        double totalPrice = 0;
        Set<ObjectId> uniqueVariantProductIds = new HashSet<>();
        for (OrderDTO.ItemDTO itemDTO : orderDTO.getItems()) {
            totalPrice += itemDTO.getDiscountPrice() * itemDTO.getQuantity();
            uniqueVariantProductIds.add(new ObjectId(itemDTO.getVariantId()));
        }

        // Tính số lượng loại sản phẩm (distinct product types)
        int totalItems = uniqueVariantProductIds.size();

        // Tính số tiền giảm giá (nếu có)
        double discountAmount = 0;
        if (orderDTO.getDiscountCode() != null && !orderDTO.getDiscountCode().isEmpty()) {
            discountAmount = calculateDiscount(orderDTO.getDiscountCode(), totalPrice, orderDTO.getUserId());
        }

        double shippingPrice = orderDTO.getShippingPrice();

        double finalPrice = totalPrice - discountAmount + shippingPrice;

        // Tạo đơn hàng mới từ OrderDTO
        Order order = Order.builder()
                .orderId(generateOrderId())
                .userId(new ObjectId(orderDTO.getUserId()))
                .items(convertItemsToModel(orderDTO.getItems()))
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .shippingPrice(shippingPrice)
                .discountCode(orderDTO.getDiscountCode())
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .phoneNumber(orderDTO.getPhoneNumber())
                .shippingAddress(convertShippingAddressToModel(orderDTO.getShippingAddress()))
                .paymentMethod(orderDTO.getPaymentMethod())
                .shippingMethod(orderDTO.getShippingMethod())
                .paymentStatus("Chưa thanh toán")
                .orderStatus("Chờ xác nhận")
                .isDeleted(false)
                .orderNotes(orderDTO.getOrderNotes())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        // Lưu đơn hàng vào cơ sở dữ liệu
        orderRepository.save(order);

        // Cập nhật tồn kho sau khi đơn hàng được tạo
        decreaseStockAfterOrder(orderDTO);

        // Trả về OrderDTO đã được tạo
        return convertToDTO(order);
    }

    private void decreaseStockAfterOrder(OrderDTO orderDTO) {
        for (OrderDTO.ItemDTO itemDTO : orderDTO.getItems()) {
            Optional<Product> optionalProduct = productRepository.findByIdAndIsDeletedFalse(itemDTO.getProductId());

            // Kiểm tra nếu sản phẩm tồn tại
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();

                // Tìm biến thể tương ứng với variantId
                for (Product.Variant variant : product.getVariants()) {
                    if (variant.getVariantId().toString().equals(itemDTO.getVariantId())) {
                        int updatedStock = variant.getStock() - itemDTO.getQuantity();
                        variant.setStock(updatedStock);

                        // Lưu lại cập nhật vào cơ sở dữ liệu
                        product.setTotalSold(product.getTotalSold() + itemDTO.getQuantity());
                        productRepository.save(product);
                        break;
                    }
                }

            } else {
                System.out.println("Sản phẩm không tìm thấy: " + itemDTO.getProductId());
            }
        }
    }

    private double calculateDiscount(String discountCode, double totalPrice, String userId) {
        // Lấy mã giảm giá từ cơ sở dữ liệu
        DiscountCode discount = discountCodeRepository.findByCodeAndIsActiveTrueAndIsDeletedFalseAndValidDateRange(discountCode, new Date())
                .orElse(null);

        // Nếu không tìm thấy mã giảm giá hợp lệ
        if (discount == null) {
            return 0;
        }

        // Kiểm tra xem người dùng đã sử dụng mã giảm giá chưa
        if (discount.getUsedByUsers() != null && discount.getUsedByUsers().contains(new ObjectId(userId))) {
            throw new RuntimeException("Mã giảm giá này đã được sử dụng bởi bạn trước đó.");
        }

        // Kiểm tra nếu giá trị đơn hàng (totalPrice) nhỏ hơn số tiền mua tối thiểu để áp dụng mã giảm giá
        if (discount.getMinimumPurchaseAmount() != null && totalPrice < discount.getMinimumPurchaseAmount()) {
            return 0;
        }

        // Kiểm tra loại giảm giá và tính toán giảm giá phù hợp
        double discountAmount = 0;
        if ("percentage".equals(discount.getDiscountType())) {
            discountAmount = totalPrice * (discount.getDiscountValue() / 100);
        } else if ("fixed".equals(discount.getDiscountType())) {
            discountAmount = discount.getDiscountValue();
        }

        discountAmount = Math.min(discountAmount, totalPrice);

        // Cập nhật usageCount và thêm userId vào usedByUsers
        discount.setUsageCount(discount.getUsageCount() + 1);
        if (discount.getUsedByUsers() == null) {
            discount.setUsedByUsers(new ArrayList<>());
        }
        discount.getUsedByUsers().add(new ObjectId(userId));

        // Lưu lại mã giảm giá với các cập nhật
        discountCodeRepository.save(discount);

        return discountAmount;
    }

    // Phương thức kiểm tra và cập nhật trạng thái đơn hàng mỗi 30 phút
    @Scheduled(fixedRate = 1800000) // Chạy mỗi 30 phút (1800000 ms)
    public void updateOrderStatusToWaitingForPickup() {
        Date currentTime = new Date();
        long thirtyMinutesAgo = currentTime.getTime() - (30 * 60 * 1000);

        List<Order> orders = orderRepository.findByOrderStatusAndCreatedAtBefore("Chờ xác nhận", new Date(thirtyMinutesAgo));
        for (Order order : orders) {
            order.setOrderStatus("Chờ lấy hàng");
            order.setUpdatedAt(new Date());

            orderRepository.save(order);
        }
    }

    // Tạo ID đơn hàng duy nhất
    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8);
    }

    // Phương thức cập nhật thông tin đơn hàng
    public OrderDTO updateOrder(String orderId, OrderDTO updatedOrderDTO) {
        // Tìm đơn hàng theo orderId
        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại hoặc đã bị xóa"));

        // Kiểm tra trạng thái đơn hàng trước khi cập nhật
        if ("Đã hủy".equals(order.getOrderStatus())) {
            throw new RuntimeException("Đơn hàng đã bị hủy, không thể cập nhật.");
        }

        updateStockAndTotalSoldForUpdatedOrder(order, updatedOrderDTO);

        if (updatedOrderDTO.getItems() != null) {
            order.setItems(convertItemsToModel(updatedOrderDTO.getItems()));
        }

        if (updatedOrderDTO.getShippingAddress() != null) {
            order.setShippingAddress(convertShippingAddressToModel(updatedOrderDTO.getShippingAddress()));
        }

        if (updatedOrderDTO.getPhoneNumber() != null) {
            order.setPhoneNumber(updatedOrderDTO.getPhoneNumber());
        }

        if (updatedOrderDTO.getOrderStatus() != null) {
            if ("Đã giao".equals(updatedOrderDTO.getOrderStatus()) || "Đã nhận hàng".equals(updatedOrderDTO.getOrderStatus())) {
                throw new RuntimeException("Không thể thay đổi trạng thái khi đơn hàng đã giao hoặc đã nhận hàng.");
            }
            order.setOrderStatus(updatedOrderDTO.getOrderStatus());
        }

        if (updatedOrderDTO.getPaymentMethod() != null) {
            order.setPaymentMethod(updatedOrderDTO.getPaymentMethod());
        }

        if (updatedOrderDTO.getShippingMethod() != null) {
            order.setShippingMethod(updatedOrderDTO.getShippingMethod());
        }

        if (updatedOrderDTO.getOrderNotes() != null) {
            order.setOrderNotes(updatedOrderDTO.getOrderNotes());
        }

        // Tính toán lại tổng giá trị của đơn hàng sau khi cập nhật
        double totalPrice = 0;
        Set<ObjectId> uniqueVariantProductIds = new HashSet<>();
        for (OrderDTO.ItemDTO itemDTO : updatedOrderDTO.getItems()) {
            totalPrice += itemDTO.getDiscountPrice() * itemDTO.getQuantity();
            uniqueVariantProductIds.add(new ObjectId(itemDTO.getVariantId()));
        }

        // Tính số lượng loại sản phẩm (distinct product types)
        int totalItems = uniqueVariantProductIds.size();

        // Tính lại giá trị giảm giá (nếu có)
        double discountAmount = 0;
        if (updatedOrderDTO.getDiscountCode() != null && !updatedOrderDTO.getDiscountCode().isEmpty()) {
            discountAmount = calculateDiscount(updatedOrderDTO.getDiscountCode(), totalPrice, updatedOrderDTO.getUserId());
        }

        double shippingPrice = updatedOrderDTO.getShippingPrice();
        double finalPrice = totalPrice - discountAmount + shippingPrice;

        // Cập nhật các thông tin liên quan đến giá trị tổng quan đơn hàng
        order.setTotalPrice(totalPrice);
        order.setTotalItems(totalItems);
        order.setDiscountCode(updatedOrderDTO.getDiscountCode());
        order.setDiscountAmount(discountAmount);
        order.setFinalPrice(finalPrice);
        order.setShippingPrice(shippingPrice);

        // Cập nhật thời gian cập nhật đơn hàng
        order.setUpdatedAt(new Date());

        // Lưu lại đơn hàng đã được cập nhật vào cơ sở dữ liệu
        orderRepository.save(order);

        return convertToDTO(order);
    }

    public void updateStockAndTotalSoldForUpdatedOrder(Order oldOrder, OrderDTO updatedOrderDTO) {
        // Duyệt qua các sản phẩm trong đơn hàng cũ và đơn hàng mới
        for (int i = 0; i < updatedOrderDTO.getItems().size(); i++) {
            OrderDTO.ItemDTO updatedItemDTO = updatedOrderDTO.getItems().get(i);
            Order.Item oldItem = oldOrder.getItems().get(i);

            // Kiểm tra sự thay đổi về số lượng
            int oldQuantity = oldItem.getQuantity();
            int newQuantity = updatedItemDTO.getQuantity();

            if (oldQuantity != newQuantity) {
                int quantityDifference = newQuantity - oldQuantity;

                Optional<Product> optionalProduct = productRepository.findByIdAndIsDeletedFalse(oldItem.getProductId().toString());

                if (optionalProduct.isPresent()) {
                    Product product = optionalProduct.get();

                    for (Product.Variant variant : product.getVariants()) {
                        if (variant.getVariantId().toString().equals(updatedItemDTO.getVariantId())) {
                            if (quantityDifference > 0) {
                                variant.setStock(variant.getStock() - quantityDifference);
                                product.setTotalSold(product.getTotalSold() + quantityDifference);
                            }
                            else {
                                variant.setStock(variant.getStock() + Math.abs(quantityDifference));
                                product.setTotalSold(product.getTotalSold() - Math.abs(quantityDifference));
                            }

                            productRepository.save(product);
                            break;
                        }
                    }
                }
            }
        }
    }

    // Phương thức hủy đơn hàng
    public OrderDTO cancelOrder(String orderId) {
        // Tìm đơn hàng theo id
        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại hoặc đã bị xóa"));

        // Kiểm tra trạng thái đơn hàng trước khi hủy
        if (!"Chờ xác nhận".equals(order.getOrderStatus())) {
            throw new RuntimeException("Chỉ các đơn hàng ở trạng thái 'Chờ xác nhận' mới có thể hủy");
        }

        // Cập nhật lại tồn kho (tăng lại số lượng khi đơn hàng bị hủy)
        increaseStockForOrder(order);

        order.setOrderStatus("Đã hủy");
        order.setDeleted(true);
        order.setUpdatedAt(new Date());

        // Lưu lại đơn hàng đã cập nhật
        orderRepository.save(order);
        return convertToDTO(order);
    }

    // Phương thức xóa đơn hàng
    public OrderDTO deleteOrder(String orderId) {
        // Tìm đơn hàng theo id
        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại hoặc đã bị xóa"));

        // Kiểm tra trạng thái đơn hàng trước khi xóa
        if ("Đã hủy".equals(order.getOrderStatus())) {
            throw new RuntimeException("Đơn hàng đã bị hủy, không thể xóa.");
        }

        // Cập nhật lại tồn kho (tăng lại số lượng khi xóa đơn hàng)
        increaseStockForOrder(order);

        // Đánh dấu đơn hàng là đã xóa
        order.setDeleted(true);
        order.setUpdatedAt(new Date());

        // Lưu lại đơn hàng đã được cập nhật
        orderRepository.save(order);
        return convertToDTO(order);
    }

    private void increaseStockForOrder(Order order) {
        for (Order.Item item : order.getItems()) {
            Optional<Product> optionalProduct = productRepository.findByIdAndIsDeletedFalse(item.getProductId().toString());
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                for (Product.Variant variant : product.getVariants()) {
                    if (variant.getVariantId().toString().equals(item.getVariantId().toString())) {
                        variant.setStock(variant.getStock() + item.getQuantity());
                        product.setTotalSold(product.getTotalSold() - item.getQuantity());
                        productRepository.save(product);
                        break;
                    }
                }
            }
        }
    }

    // Xuất tất cả đơn hàng ra file Excel
    public ByteArrayOutputStream exportOrdersToExcel(String paymentMethod, String paymentStatus, String orderStatus, String shippingMethod, String sortBy) throws IOException {
        // Lấy tất cả đơn hàng từ repository (dữ liệu từ OrderDTO)
        List<OrderDTO> orders = getAllOrders(paymentMethod, paymentStatus, orderStatus, shippingMethod, sortBy);

        // Tạo workbook Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Orders");

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
        String[] columns = {
                "ID", "Mã đơn hàng", "Mã người dùng", "Tổng số sản phẩm", "Tổng giá trị",
                "Giá vận chuyển", "Mã giảm giá", "Số tiền giảm giá", "Giá trị cuối", "Số điện thoại",
                "Địa chỉ giao hàng", "Phương thức thanh toán", "Trạng thái thanh toán", "Trạng thái đơn hàng",
                "Phương thức giao hàng", "Ngày tạo", "Ngày cập nhật", "Trạng thái xóa", "Ghi chú"
        };
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Định dạng cho dữ liệu
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Thêm dữ liệu đơn hàng vào file Excel
        int rowNum = 1;
        for (OrderDTO order : orders) {
            Row row = sheet.createRow(rowNum++);

            // Cột ID
            row.createCell(0).setCellValue(order.getId());
            row.getCell(0).setCellStyle(dataStyle);

            // Cột Mã đơn hàng
            row.createCell(1).setCellValue(order.getOrderId());
            row.getCell(1).setCellStyle(dataStyle);

            // Cột Mã người dùng
            row.createCell(2).setCellValue(order.getUserId());
            row.getCell(2).setCellStyle(dataStyle);

            // Cột Tổng số sản phẩm
            row.createCell(3).setCellValue(order.getTotalItems());
            row.getCell(3).setCellStyle(dataStyle);

            // Cột Tổng giá trị
            row.createCell(4).setCellValue(order.getTotalPrice());
            row.getCell(4).setCellStyle(dataStyle);

            // Cột Giá vận chuyển
            row.createCell(5).setCellValue(order.getShippingPrice());
            row.getCell(5).setCellStyle(dataStyle);

            // Cột Mã giảm giá
            row.createCell(6).setCellValue(order.getDiscountCode() != null ? order.getDiscountCode() : "N/A");
            row.getCell(6).setCellStyle(dataStyle);

            // Cột Số tiền giảm giá
            row.createCell(7).setCellValue(order.getDiscountAmount());
            row.getCell(7).setCellStyle(dataStyle);

            // Cột Giá trị cuối
            row.createCell(8).setCellValue(order.getFinalPrice());
            row.getCell(8).setCellStyle(dataStyle);

            // Cột Số điện thoại
            row.createCell(9).setCellValue(order.getPhoneNumber());
            row.getCell(9).setCellStyle(dataStyle);

            // Cột Địa chỉ giao hàng
            row.createCell(10).setCellValue(order.getShippingAddress() != null ? order.getShippingAddress().toString() : "N/A");
            row.getCell(10).setCellStyle(dataStyle);

            // Cột Phương thức thanh toán
            row.createCell(11).setCellValue(order.getPaymentMethod());
            row.getCell(11).setCellStyle(dataStyle);

            // Cột Trạng thái thanh toán
            row.createCell(12).setCellValue(order.getPaymentStatus());
            row.getCell(12).setCellStyle(dataStyle);

            // Cột Trạng thái đơn hàng
            row.createCell(13).setCellValue(order.getOrderStatus());
            row.getCell(13).setCellStyle(dataStyle);

            // Cột Phương thức giao hàng
            row.createCell(14).setCellValue(order.getShippingMethod());
            row.getCell(14).setCellStyle(dataStyle);

            // Cột Ngày tạo
            row.createCell(15).setCellValue(order.getCreatedAt() != null ? order.getCreatedAt().toString() : "N/A");
            row.getCell(15).setCellStyle(dataStyle);

            // Cột Ngày cập nhật
            row.createCell(16).setCellValue(order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : "N/A");
            row.getCell(16).setCellStyle(dataStyle);

            // Cột Trạng thái xóa
            row.createCell(17).setCellValue(order.isDeleted() ? "Đã xóa" : "Đang hiển thị");
            row.getCell(17).setCellStyle(dataStyle);

            // Cột Ghi chú
            row.createCell(18).setCellValue(order.getOrderNotes() != null ? order.getOrderNotes() : "N/A");
            row.getCell(18).setCellStyle(dataStyle);
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

    // Chuyển đổi từ Order sang OrderDTO
    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .userId(order.getUserId().toString())
                .items(order.getItems().stream().map(item -> OrderDTO.ItemDTO.builder()
                                .productId(item.getProductId().toString())
                                .variantId(item.getVariantId().toString())
                                .brandId(item.getBrandId().toString())
                                .categoryId(item.getCategoryId().toString())
                                .discountPrice(item.getDiscountPrice())
                                .originalPrice(item.getOriginalPrice())
                                .quantity(item.getQuantity())
                                .totalPrice(item.getTotalPrice())
                                .productName(item.getProductName())
                                .color(item.getColor())
                                .hexCode(item.getHexCode())
                                .size(item.getSize())
                                .ram(item.getRam())
                                .storage(item.getStorage())
                                .condition(item.getCondition())
                                .productImage(item.getProductImage())
                                .createdAt(item.getCreatedAt())
                                .updatedAt(item.getUpdatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .totalItems(order.getTotalItems())
                .totalPrice(order.getTotalPrice())
                .shippingPrice(order.getShippingPrice())
                .discountCode(order.getDiscountCode())
                .discountAmount(order.getDiscountAmount())
                .finalPrice(order.getFinalPrice())
                .phoneNumber(order.getPhoneNumber())
                .shippingAddress(OrderDTO.ShippingAddressDTO.builder()
                        .street(order.getShippingAddress().getStreet())
                        .communes(order.getShippingAddress().getCommunes())
                        .district(order.getShippingAddress().getDistrict())
                        .city(order.getShippingAddress().getCity())
                        .country(order.getShippingAddress().getCountry())
                        .build())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .shippingMethod(order.getShippingMethod())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .isDeleted(order.isDeleted())
                .orderNotes(order.getOrderNotes())
                .build();
    }

    // Chuyển đổi các ItemDTO sang Item trong Order
    private List<Order.Item> convertItemsToModel(List<OrderDTO.ItemDTO> itemDTOs) {
        return itemDTOs.stream().map(itemDTO -> Order.Item.builder()
                        .productId(new ObjectId(itemDTO.getProductId()))
                        .variantId(new ObjectId(itemDTO.getVariantId()))
                        .brandId(new ObjectId(itemDTO.getBrandId()))
                        .categoryId(new ObjectId(itemDTO.getCategoryId()))
                        .discountPrice(itemDTO.getDiscountPrice())
                        .originalPrice(itemDTO.getOriginalPrice())
                        .quantity(itemDTO.getQuantity())
                        .totalPrice(itemDTO.getTotalPrice())
                        .productName(itemDTO.getProductName())
                        .color(itemDTO.getColor())
                        .hexCode(itemDTO.getHexCode())
                        .size(itemDTO.getSize())
                        .ram(itemDTO.getRam())
                        .storage(itemDTO.getStorage())
                        .condition(itemDTO.getCondition())
                        .productImage(itemDTO.getProductImage())
                        .createdAt(itemDTO.getCreatedAt())
                        .updatedAt(itemDTO.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Chuyển đổi ShippingAddressDTO sang ShippingAddress
    private Order.ShippingAddress convertShippingAddressToModel(OrderDTO.ShippingAddressDTO shippingAddressDTO) {
        return Order.ShippingAddress.builder()
                .street(shippingAddressDTO.getStreet())
                .communes(shippingAddressDTO.getCommunes())
                .district(shippingAddressDTO.getDistrict())
                .city(shippingAddressDTO.getCity())
                .country(shippingAddressDTO.getCountry())
                .build();
    }
}