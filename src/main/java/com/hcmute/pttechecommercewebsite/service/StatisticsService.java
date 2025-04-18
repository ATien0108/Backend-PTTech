package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.StatisticsDTO;
import com.hcmute.pttechecommercewebsite.model.Review;
import com.hcmute.pttechecommercewebsite.model.Statistics;
import com.hcmute.pttechecommercewebsite.model.Order;
import com.hcmute.pttechecommercewebsite.model.Product;
import com.hcmute.pttechecommercewebsite.repository.OrderRepository;
import com.hcmute.pttechecommercewebsite.repository.ProductRepository;
import com.hcmute.pttechecommercewebsite.repository.ReviewRepository;
import com.hcmute.pttechecommercewebsite.repository.StatisticsRepository;
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
public class StatisticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;

    // Lọc thống kê theo sortBy và period
    public List<StatisticsDTO> getAllStatistics(String sortBy, String period) {
        List<Statistics> statisticsList = statisticsRepository.findAll();

        // Lọc thống kê theo thời gian
        if (period != null) {
            Date startDate = getStartDateForPeriod(period);
            statisticsList = statisticsList.stream()
                    .filter(stat -> stat.getDate().after(startDate))
                    .collect(Collectors.toList());
        }

        // Sắp xếp theo 'newest' hoặc 'oldest'
        if ("latest".equals(sortBy)) {
            statisticsList.sort((s1, s2) -> s2.getDate().compareTo(s1.getDate()));
        } else if ("oldest".equals(sortBy)) {
            statisticsList.sort(Comparator.comparing(Statistics::getDate));
        }

        // Chuyển đổi sang DTO
        return statisticsList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy chi tiết thống kê theo ID
    public StatisticsDTO getStatisticsById(String id) {
        Optional<Statistics> statisticsOptional = statisticsRepository.findById(id);

        if (statisticsOptional.isPresent()) {
            return convertToDTO(statisticsOptional.get());
        } else {
            throw new RuntimeException("Không tìm thấy số liệu thống kê với id: " + id);
        }
    }

    // Lấy ngày bắt đầu của tuần, tháng hoặc năm
    private Date getStartDateForPeriod(String period) {
        Calendar calendar = Calendar.getInstance();
        switch (period) {
            case "week":
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case "month":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "year":
                calendar.set(Calendar.MONTH, Calendar.JANUARY);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                break;
            default:
                return new Date(0);
        }
        return calendar.getTime();
    }

    // Hàm để thực hiện thống kê vào cuối ngày
    @Scheduled(cron = "0 * * * * ?") // Thực hiện mỗi phút
    public void generateDailyStatistics() {
        Date today = new Date();
        Date startOfDay = getStartOfDay(today);
        Date endOfDay = getEndOfDay(today);

        // Kiểm tra xem đã có thống kê cho ngày hôm nay chưa (so sánh trong khoảng thời gian ngày)
        Optional<Statistics> existingStatistics = statisticsRepository.findByDateBetween(startOfDay, endOfDay);

        // Lấy tất cả các đơn hàng trong ngày
        List<Order> orders = orderRepository.findByCreatedAtBetween(startOfDay, endOfDay);

        // Tính toán các thông số thống kê
        int totalOrders = orders.size();
        int totalItemsSold = 0;
        double totalRevenue = 0;
        double totalDiscounts = 0;
        double totalPaymentAmount = 0;
        double totalShippingCosts = 0;
        double totalPaymentFees = 0;
        List<Statistics.ProductSales> topSellingProducts = new ArrayList<>();
        int newCustomers = 0;
        int returningCustomers = 0;
        Map<String, Integer> totalOrdersByPaymentMethod = new HashMap<>();
        Map<String, Integer> totalOrdersByStatus = new HashMap<>();
        Map<String, Integer> totalOrdersByShippingMethod = new HashMap<>();
        int totalReturnedItems = 0;
        double totalRevenueFromReturns = 0;

        double highestOrderValue = Double.MIN_VALUE;
        double lowestOrderValue = Double.MAX_VALUE;

        // Đếm số lượng sản phẩm bán ra và tổng doanh thu
        Map<ObjectId, Integer> productSales = new HashMap<>();

        // Các biến cho Customer Feedback
        int totalReviews = 0;
        int positiveReviews = 0;
        int negativeReviews = 0;
        double totalRating = 0;

        // Lấy tất cả các đánh giá của sản phẩm trong ngày hôm nay
        List<Review> reviews = reviewRepository.findByCreatedAtBetween(startOfDay, endOfDay);

        // Đếm các đánh giá cho các sản phẩm trong ngày hôm nay
        for (Review review : reviews) {
            totalReviews++;
            totalRating += review.getRating();
            if (review.getRating() >= 4) {
                positiveReviews++;
            } else if (review.getRating() <= 2) {
                negativeReviews++;
            }
        }

        double averageRating = (totalReviews > 0) ? totalRating / totalReviews : 0;

        // Đếm số lượng sản phẩm bán ra và tính toán doanh thu
        for (Order order : orders) {
            totalPaymentAmount += order.getFinalPrice();
            totalShippingCosts += order.getShippingPrice();
            totalPaymentFees += order.getFinalPrice();
            totalRevenue += order.getFinalPrice();
            totalDiscounts += order.getDiscountAmount();
            totalItemsSold += order.getTotalItems();

            // Tính giá trị trung bình đơn hàng, giá trị cao nhất và thấp nhất
            if (order.getFinalPrice() > highestOrderValue) {
                highestOrderValue = order.getFinalPrice();
            }
            if (order.getFinalPrice() < lowestOrderValue) {
                lowestOrderValue = order.getFinalPrice();
            }

            for (Order.Item item : order.getItems()) {
                // Cập nhật doanh thu cho mỗi sản phẩm
                productSales.put(item.getProductId(), productSales.getOrDefault(item.getProductId(), 0) + item.getQuantity());
            }

            // Phân loại theo phương thức thanh toán
            totalOrdersByPaymentMethod.put(order.getPaymentMethod(), totalOrdersByPaymentMethod.getOrDefault(order.getPaymentMethod(), 0) + 1);

            // Phân loại theo trạng thái đơn hàng
            totalOrdersByStatus.put(order.getOrderStatus(), totalOrdersByStatus.getOrDefault(order.getOrderStatus(), 0) + 1);

            // Phân loại theo phương thức vận chuyển
            totalOrdersByShippingMethod.put(order.getShippingMethod(), totalOrdersByShippingMethod.getOrDefault(order.getShippingMethod(), 0) + 1);

            // Tính toán số khách hàng quay lại
            if (isReturningCustomer(order)) {
                returningCustomers++;
            } else {
                newCustomers++;
            }

            // Kiểm tra các đơn hàng trả lại
            if (order.getOrderStatus().equals("Trả hàng")) {
                totalReturnedItems += order.getTotalItems();
                totalRevenueFromReturns += order.getFinalPrice();
            }
        }

        // Lấy top sản phẩm bán chạy
        for (Map.Entry<ObjectId, Integer> entry : productSales.entrySet()) {
            Product product = productRepository.findById(entry.getKey().toString()).orElse(null);
            if (product != null) {
                topSellingProducts.add(new Statistics.ProductSales(entry.getKey(), product.getName(), entry.getValue(), product.getPricing().getCurrent() * entry.getValue()));
            }
        }

        // Tính toán giá trị trung bình của đơn hàng
        double averageOrderValue = totalRevenue / totalOrders;

        // Nếu đã có thống kê cho ngày hôm nay, cập nhật lại
        if (existingStatistics.isPresent()) {
            Statistics existingStat = existingStatistics.get();

            // Cập nhật các trường thống kê
            existingStat.setTotalOrders(totalOrders);
            existingStat.setTotalItemsSold(totalItemsSold);
            existingStat.setTotalRevenue(totalRevenue);
            existingStat.setTotalDiscounts(totalDiscounts);
            existingStat.setTotalPaymentAmount(totalPaymentAmount);
            existingStat.setTotalShippingCosts(totalShippingCosts);
            existingStat.setTotalPaymentFees(totalPaymentFees);
            existingStat.setTopSellingProducts(topSellingProducts);
            existingStat.setNewCustomers(newCustomers);
            existingStat.setTotalCustomers(newCustomers + returningCustomers);
            existingStat.setReturningCustomers(returningCustomers);
            existingStat.setTotalOrdersByPaymentMethod(totalOrdersByPaymentMethod);
            existingStat.setTotalOrdersByStatus(totalOrdersByStatus);
            existingStat.setTotalOrdersByShippingMethod(totalOrdersByShippingMethod);
            existingStat.setTotalReturnedItems(totalReturnedItems);
            existingStat.setTotalRevenueFromReturns(totalRevenueFromReturns);
            existingStat.setAverageOrderValue(averageOrderValue);
            existingStat.setHighestOrderValue(highestOrderValue);
            existingStat.setLowestOrderValue(lowestOrderValue);

            // Cập nhật Customer Feedback
            Statistics.CustomerFeedback customerFeedback = new Statistics.CustomerFeedback(averageRating, totalReviews, positiveReviews, negativeReviews);
            existingStat.setCustomerFeedback(customerFeedback);

            // Lưu lại bản cập nhật
            statisticsRepository.save(existingStat);
        } else {
            // Tạo mới nếu chưa có thống kê cho ngày hôm nay
            Statistics statistics = Statistics.builder()
                    .date(today)
                    .totalOrders(totalOrders)
                    .totalItemsSold(totalItemsSold)
                    .totalRevenue(totalRevenue)
                    .totalDiscounts(totalDiscounts)
                    .totalPaymentAmount(totalPaymentAmount)
                    .totalShippingCosts(totalShippingCosts)
                    .totalPaymentFees(totalPaymentFees)
                    .topSellingProducts(topSellingProducts)
                    .newCustomers(newCustomers)
                    .totalCustomers(newCustomers + returningCustomers)
                    .returningCustomers(returningCustomers)
                    .totalOrdersByPaymentMethod(totalOrdersByPaymentMethod)
                    .totalOrdersByStatus(totalOrdersByStatus)
                    .totalOrdersByShippingMethod(totalOrdersByShippingMethod)
                    .totalReturnedItems(totalReturnedItems)
                    .totalRevenueFromReturns(totalRevenueFromReturns)
                    .averageOrderValue(averageOrderValue)
                    .highestOrderValue(highestOrderValue)
                    .lowestOrderValue(lowestOrderValue)
                    .customerFeedback(new Statistics.CustomerFeedback(averageRating, totalReviews, positiveReviews, negativeReviews))
                    .build();

            // Lưu thống kê mới
            statisticsRepository.save(statistics);
        }
    }

    // Hàm để lấy thời gian bắt đầu của ngày
    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // Hàm để lấy thời gian kết thúc của ngày
    private Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    // Hàm kiểm tra khách hàng có quay lại hay không
    private boolean isReturningCustomer(Order order) {
        return orderRepository.findByUserIdAndIsDeletedFalse(order.getUserId()).size() > 1;
    }

    public ByteArrayOutputStream exportStatisticsToExcel(String sortBy, String period) throws IOException {
        List<StatisticsDTO> statisticsList = getAllStatistics(sortBy, period);

        // Tạo workbook Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Statistics");

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
                "ID", "Ngày thống kê", "Tổng số đơn hàng", "Tổng số sản phẩm bán", "Tổng doanh thu", "Tổng giảm giá",
                "Tổng thanh toán", "Tổng chi phí vận chuyển", "Tổng phí thanh toán", "Sản phẩm bán chạy", "Khách hàng mới",
                "Tổng khách hàng", "Khách hàng quay lại", "Đánh giá trung bình", "Tổng số đánh giá", "Đánh giá tích cực",
                "Đánh giá tiêu cực", "Giá trị trung bình của đơn hàng", "Giá trị cao nhất của đơn hàng",
                "Giá trị thấp nhất của đơn hàng", "Tổng số sản phẩm trả lại", "Doanh thu từ sản phẩm trả lại"
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

        // Thêm dữ liệu thống kê vào file Excel
        int rowNum = 1;
        for (StatisticsDTO statistics : statisticsList) {
            Row row = sheet.createRow(rowNum++);

            // Cột ID
            row.createCell(0).setCellValue(statistics.getId());
            row.getCell(0).setCellStyle(dataStyle);

            // Cột Ngày thống kê
            row.createCell(1).setCellValue(statistics.getDate().toString());
            row.getCell(1).setCellStyle(dataStyle);

            // Cột Tổng số đơn hàng
            row.createCell(2).setCellValue(statistics.getTotalOrders());
            row.getCell(2).setCellStyle(dataStyle);

            // Cột Tổng số sản phẩm bán
            row.createCell(3).setCellValue(statistics.getTotalItemsSold());
            row.getCell(3).setCellStyle(dataStyle);

            // Cột Tổng doanh thu
            row.createCell(4).setCellValue(statistics.getTotalRevenue());
            row.getCell(4).setCellStyle(dataStyle);

            // Cột Tổng giảm giá
            row.createCell(5).setCellValue(statistics.getTotalDiscounts());
            row.getCell(5).setCellStyle(dataStyle);

            // Cột Tổng thanh toán
            row.createCell(6).setCellValue(statistics.getTotalPaymentAmount());
            row.getCell(6).setCellStyle(dataStyle);

            // Cột Tổng chi phí vận chuyển
            row.createCell(7).setCellValue(statistics.getTotalShippingCosts());
            row.getCell(7).setCellStyle(dataStyle);

            // Cột Tổng phí thanh toán
            row.createCell(8).setCellValue(statistics.getTotalPaymentFees());
            row.getCell(8).setCellStyle(dataStyle);

            // Cột Sản phẩm bán chạy (Tạo chuỗi từ topSellingProducts)
            row.createCell(9).setCellValue(getTopSellingProductsString(statistics.getTopSellingProducts()));
            row.getCell(9).setCellStyle(dataStyle);

            // Cột Khách hàng mới
            row.createCell(10).setCellValue(statistics.getNewCustomers());
            row.getCell(10).setCellStyle(dataStyle);

            // Cột Tổng khách hàng
            row.createCell(11).setCellValue(statistics.getTotalCustomers());
            row.getCell(11).setCellStyle(dataStyle);

            // Cột Khách hàng quay lại
            row.createCell(12).setCellValue(statistics.getReturningCustomers());
            row.getCell(12).setCellStyle(dataStyle);

            // Cột Đánh giá trung bình
            row.createCell(13).setCellValue(statistics.getCustomerFeedback().getAverageRating());
            row.getCell(13).setCellStyle(dataStyle);

            // Cột Tổng số đánh giá
            row.createCell(14).setCellValue(statistics.getCustomerFeedback().getTotalReviews());
            row.getCell(14).setCellStyle(dataStyle);

            // Cột Đánh giá tích cực
            row.createCell(15).setCellValue(statistics.getCustomerFeedback().getPositiveReviews());
            row.getCell(15).setCellStyle(dataStyle);

            // Cột Đánh giá tiêu cực
            row.createCell(16).setCellValue(statistics.getCustomerFeedback().getNegativeReviews());
            row.getCell(16).setCellStyle(dataStyle);

            // Cột Giá trị trung bình của đơn hàng
            row.createCell(17).setCellValue(statistics.getAverageOrderValue());
            row.getCell(17).setCellStyle(dataStyle);

            // Cột Giá trị cao nhất của đơn hàng
            row.createCell(18).setCellValue(statistics.getHighestOrderValue());
            row.getCell(18).setCellStyle(dataStyle);

            // Cột Giá trị thấp nhất của đơn hàng
            row.createCell(19).setCellValue(statistics.getLowestOrderValue());
            row.getCell(19).setCellStyle(dataStyle);

            // Cột Tổng số sản phẩm trả lại
            row.createCell(20).setCellValue(statistics.getTotalReturnedItems());
            row.getCell(20).setCellStyle(dataStyle);

            // Cột Doanh thu từ sản phẩm trả lại
            row.createCell(21).setCellValue(statistics.getTotalRevenueFromReturns());
            row.getCell(21).setCellStyle(dataStyle);
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

    // Phương thức để lấy danh sách sản phẩm bán chạy dưới dạng chuỗi
    private String getTopSellingProductsString(List<StatisticsDTO.ProductSalesDTO> topSellingProducts) {
        StringBuilder sb = new StringBuilder();
        for (StatisticsDTO.ProductSalesDTO product : topSellingProducts) {
            sb.append(product.getProductName())
                    .append(" (")
                    .append(product.getQuantitySold())
                    .append(" sold), ");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "";
    }

    // Phương thức chuyển đổi từ Statistics sang StatisticsDTO
    private StatisticsDTO convertToDTO(Statistics statistics) {
        return StatisticsDTO.builder()
                .id(statistics.getId().toString())
                .date(statistics.getDate())
                .totalOrders(statistics.getTotalOrders())
                .totalItemsSold(statistics.getTotalItemsSold())
                .totalRevenue(statistics.getTotalRevenue())
                .totalDiscounts(statistics.getTotalDiscounts())
                .totalPaymentAmount(statistics.getTotalPaymentAmount())
                .totalShippingCosts(statistics.getTotalShippingCosts())
                .totalPaymentFees(statistics.getTotalPaymentFees())
                .topSellingProducts(convertToProductSalesDTO(statistics.getTopSellingProducts()))
                .newCustomers(statistics.getNewCustomers())
                .totalCustomers(statistics.getTotalCustomers())
                .returningCustomers(statistics.getReturningCustomers())
                .customerFeedback(convertToCustomerFeedbackDTO(statistics.getCustomerFeedback()))
                .totalOrdersByPaymentMethod(statistics.getTotalOrdersByPaymentMethod())
                .totalOrdersByStatus(statistics.getTotalOrdersByStatus())
                .totalOrdersByShippingMethod(statistics.getTotalOrdersByShippingMethod())
                .totalReturnedItems(statistics.getTotalReturnedItems())
                .totalRevenueFromReturns(statistics.getTotalRevenueFromReturns())
                .averageOrderValue(statistics.getAverageOrderValue())
                .highestOrderValue(statistics.getHighestOrderValue())
                .lowestOrderValue(statistics.getLowestOrderValue())
                .build();
    }

    // Chuyển đổi topSellingProducts
    private List<StatisticsDTO.ProductSalesDTO> convertToProductSalesDTO(List<Statistics.ProductSales> topSellingProducts) {
        return topSellingProducts.stream()
                .map(productSales -> new StatisticsDTO.ProductSalesDTO(
                        productSales.getProductId().toString(),
                        productSales.getProductName(),
                        productSales.getQuantitySold(),
                        productSales.getRevenueFromProduct()
                ))
                .collect(Collectors.toList());
    }

    // Chuyển đổi customer feedback
    private StatisticsDTO.CustomerFeedbackDTO convertToCustomerFeedbackDTO(Statistics.CustomerFeedback customerFeedback) {
        if (customerFeedback == null) {
            return null;
        }
        return StatisticsDTO.CustomerFeedbackDTO.builder()
                .averageRating(customerFeedback.getAverageRating())
                .totalReviews(customerFeedback.getTotalReviews())
                .positiveReviews(customerFeedback.getPositiveReviews())
                .negativeReviews(customerFeedback.getNegativeReviews())
                .build();
    }
}