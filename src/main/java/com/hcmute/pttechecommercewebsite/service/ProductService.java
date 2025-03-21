package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.ProductDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.model.Product;
import com.hcmute.pttechecommercewebsite.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private String uploadDir = "upload-images/products";  // Thư mục hình ảnh
    private String uploadUrl = "http://localhost:8081/images/products";  // URL công khai hình ảnh

    private String uploadVideoDir = "upload-videos/products";  // Thư mục video
    private String uploadVideoUrl = "http://localhost:8081/videos/products";  // URL công khai video

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Chuyển Entity thành DTO
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .productId(product.getProductId())
                .name(product.getName())
                .brandId(product.getBrandId() != null ? product.getBrandId().toString() : null)
                .categoryId(product.getCategoryId() != null ? product.getCategoryId().toString() : null)
                .description(product.getDescription())
                .pricing(convertPricingToDTO(product.getPricing()))
                .specifications(product.getSpecifications())
                .variants(product.getVariants().stream().map(this::convertVariantToDTO).collect(Collectors.toList()))
                .tags(product.getTags())
                .images(product.getImages())
                .videos(product.getVideos())
                .blog(convertBlogToDTO(product.getBlog()))
                .ratings(convertRatingsToDTO(product.getRatings()))
                .warranty(convertWarrantyToDTO(product.getWarranty()))
                .totalSold(product.getTotalSold())
                .status(product.getStatus())
                .visibilityType(product.getVisibilityType())
                .isDeleted(product.isDeleted())
                .scheduledDate(product.getScheduledDate())
                .build();
    }

    // Chuyển Pricing thành DTO
    private ProductDTO.PricingDTO convertPricingToDTO(Product.Pricing pricing) {
        return ProductDTO.PricingDTO.builder()
                .original(pricing.getOriginal())
                .current(pricing.getCurrent())
                .history(pricing.getHistory().stream()
                        .map(history -> new ProductDTO.PricingDTO.PriceHistoryDTO(history.getPreviousPrice(), history.getNewPrice(), history.getChangedAt()))
                        .collect(Collectors.toList()))
                .build();
    }

    // Chuyển Variant thành DTO
    private ProductDTO.VariantDTO convertVariantToDTO(Product.Variant variant) {
        return ProductDTO.VariantDTO.builder()
                .variantId(variant.getVariantId().toString())
                .color(variant.getColor())
                .hexCode(variant.getHexCode())
                .size(variant.getSize())
                .ram(variant.getRam())
                .storage(variant.getStorage())
                .condition(variant.getCondition())
                .stock(variant.getStock())
                .build();
    }

    // Chuyển Blog thành DTO
    private ProductDTO.BlogDTO convertBlogToDTO(Product.Blog blog) {
        return ProductDTO.BlogDTO.builder()
                .title(blog.getTitle())
                .description(blog.getDescription())
                .content(blog.getContent())
                .publishedDate(blog.getPublishedDate())
                .build();
    }

    // Chuyển Ratings thành DTO
    private ProductDTO.RatingsDTO convertRatingsToDTO(Product.Ratings ratings) {
        return ProductDTO.RatingsDTO.builder()
                .average(ratings.getAverage())
                .totalReviews(ratings.getTotalReviews())
                .build();
    }

    // Chuyển Warranty thành DTO
    private ProductDTO.WarrantyDTO convertWarrantyToDTO(Product.Warranty warranty) {
        return ProductDTO.WarrantyDTO.builder()
                .duration(warranty.getDuration())
                .terms(warranty.getTerms())
                .build();
    }

    // Lấy tất cả sản phẩm không bị xóa
    public List<ProductDTO> getAllProducts(String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        }

        List<Product> products = productRepository.findByIsDeletedFalse(sort);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Lấy tất cả sản phẩm không bị xóa và không có trạng thái "inactive"
    public List<ProductDTO> getAllActiveProducts(String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        }

        List<Product> products = productRepository.findByIsDeletedFalseAndStatusNot("inactive", sort);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Lấy tất cả sản phẩm không bị xóa và không hiển thị
    public List<ProductDTO> getAllInactiveProducts(String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        }

        List<Product> products = productRepository.findByIsDeletedFalseAndStatus("inactive", sort);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Lấy top 10 sản phẩm bán chạy nhất (dựa vào sắp xếp giảm dần theo totalSold)
    public List<ProductDTO> getTopSellingProducts() {
        // Lấy tất cả sản phẩm chưa bị xóa
        List<Product> products = productRepository.findByIsDeletedFalse();

        // Sắp xếp sản phẩm theo totalSold giảm dần
        products.sort(Comparator.comparingInt(Product::getTotalSold).reversed());

        // Lấy ra 10 sản phẩm đầu tiên
        List<Product> topSellingProducts = products.stream()
                .limit(10)
                .collect(Collectors.toList());

        if (topSellingProducts.isEmpty()) {
            return new ArrayList<>();
        }

        return topSellingProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy top 10 sản phẩm có số lượng đánh giá và số sao cao nhất
    public List<ProductDTO> getTopRatedProducts() {
        // Lấy tất cả sản phẩm chưa bị xóa
        List<Product> products = productRepository.findByIsDeletedFalse();

        // Sắp xếp sản phẩm theo số sao trung bình (ratings.average) giảm dần
        products.sort((p1, p2) -> Double.compare(p2.getRatings().getAverage(), p1.getRatings().getAverage()));

        // Lấy ra 10 sản phẩm đầu tiên
        List<Product> topRatedProducts = products.stream()
                .limit(10)
                .collect(Collectors.toList());

        if (topRatedProducts.isEmpty()) {
            return new ArrayList<>();
        }

        return topRatedProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy những sản phẩm có số lượng tồn kho dưới 10
    public List<ProductDTO> getLowStockProducts() {
        List<Product> products = productRepository.findByVariantsStockLessThanAndIsDeletedFalse(10);

        if (products.isEmpty()) {
            return new ArrayList<>();
        }

        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Lấy sản phẩm theo ID
    public Optional<ProductDTO> getProductById(String id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(this::convertToDTO);
    }

    // Tìm kiếm sản phẩm theo tên
    public List<ProductDTO> searchProductsByName(String keyword) {
        List<Product> products = productRepository.findByNameContaining(keyword);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Thêm mới sản phẩm
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = Product.builder()
                .productId(productDTO.getProductId())
                .name(productDTO.getName())
                .brandId(productDTO.getBrandId() != null ? new ObjectId(productDTO.getBrandId()) : null)
                .categoryId(productDTO.getCategoryId() != null ? new ObjectId(productDTO.getCategoryId()) : null)
                .description(productDTO.getDescription())
                .pricing(convertPricingFromDTO(productDTO.getPricing()))
                .specifications(productDTO.getSpecifications())
                .variants(productDTO.getVariants().stream().map(this::convertVariantFromDTO).collect(Collectors.toList()))
                .tags(productDTO.getTags())
                .images(productDTO.getImages())
                .videos(productDTO.getVideos())
                .blog(convertBlogFromDTO(productDTO.getBlog()))
                .ratings(productDTO.getRatings() != null ? convertRatingsFromDTO(productDTO.getRatings()) : new Product.Ratings(0, 0))
                .warranty(convertWarrantyFromDTO(productDTO.getWarranty()))
                .totalSold(0)
                .status(productDTO.getStatus())
                .visibilityType("Mới")
                .isDeleted(false)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    // Chuyển Pricing từ DTO
    private Product.Pricing convertPricingFromDTO(ProductDTO.PricingDTO pricingDTO) {
        return Product.Pricing.builder()
                .original(pricingDTO.getOriginal())
                .current(pricingDTO.getCurrent())
                .history(pricingDTO.getHistory().stream()
                        .map(history -> new Product.Pricing.PriceHistory(history.getPreviousPrice(), history.getNewPrice(), history.getChangedAt()))
                        .collect(Collectors.toList()))
                .build();
    }

    // Chuyển Variant từ DTO
    private Product.Variant convertVariantFromDTO(ProductDTO.VariantDTO variantDTO) {
        return Product.Variant.builder()
                .variantId(variantDTO.getVariantId() != null ? new ObjectId(variantDTO.getVariantId()) : new ObjectId())
                .color(variantDTO.getColor())
                .hexCode(variantDTO.getHexCode())
                .size(variantDTO.getSize())
                .ram(variantDTO.getRam())
                .storage(variantDTO.getStorage())
                .condition(variantDTO.getCondition())
                .stock(variantDTO.getStock())
                .build();
    }

    // Chuyển Blog từ DTO
    private Product.Blog convertBlogFromDTO(ProductDTO.BlogDTO blogDTO) {
        return Product.Blog.builder()
                .title(blogDTO.getTitle())
                .description(blogDTO.getDescription())
                .content(blogDTO.getContent())
                .publishedDate(blogDTO.getPublishedDate())
                .build();
    }

    // Chuyển Ratings từ DTO
    private Product.Ratings convertRatingsFromDTO(ProductDTO.RatingsDTO ratingsDTO) {
        return Product.Ratings.builder()
                .average(ratingsDTO.getAverage())
                .totalReviews(ratingsDTO.getTotalReviews())
                .build();
    }

    // Chuyển Warranty từ DTO
    private Product.Warranty convertWarrantyFromDTO(ProductDTO.WarrantyDTO warrantyDTO) {
        return Product.Warranty.builder()
                .duration(warrantyDTO.getDuration())
                .terms(warrantyDTO.getTerms())
                .build();
    }

    // Tạo danh mục với thời gian lên lịch
    public ProductDTO scheduleCreateProduct(ProductDTO productDTO) {
        if (productDTO.getScheduledDate() != null && productDTO.getScheduledDate().before(new Date())) {
            throw new IllegalArgumentException("Thời gian phải trong tương lai.");
        }

        // Chuyển ProductDTO thành Product entity
        Product newProduct = Product.builder()
                .productId(productDTO.getProductId())
                .name(productDTO.getName())
                .brandId(productDTO.getBrandId() != null ? new ObjectId(productDTO.getBrandId()) : null)
                .categoryId(productDTO.getCategoryId() != null ? new ObjectId(productDTO.getCategoryId()) : null)
                .description(productDTO.getDescription())
                .pricing(convertPricingFromDTO(productDTO.getPricing()))
                .specifications(productDTO.getSpecifications())
                .variants(productDTO.getVariants().stream().map(this::convertVariantFromDTO).collect(Collectors.toList()))
                .tags(productDTO.getTags())
                .images(productDTO.getImages())
                .videos(productDTO.getVideos())
                .blog(convertBlogFromDTO(productDTO.getBlog()))
                .ratings(productDTO.getRatings() != null ? convertRatingsFromDTO(productDTO.getRatings()) : new Product.Ratings(0, 0))
                .warranty(convertWarrantyFromDTO(productDTO.getWarranty()))
                .totalSold(0)
                .status("inactive")
                .visibilityType("Mới")
                .isDeleted(false)
                .scheduledDate(productDTO.getScheduledDate())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        productRepository.save(newProduct);
        return convertToDTO(newProduct);
    }

    // Kiểm tra và kích hoạt sản phẩm khi đến thời gian lên lịch
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndActivateScheduledProducts() {
        Date now = new Date();
        List<Product> scheduledProducts = productRepository.findByScheduledDateBeforeAndIsDeletedFalse(now);

        for (Product product : scheduledProducts) {
            product.setStatus("active");
            product.setScheduledDate(null);
            product.setUpdatedAt(now);
            productRepository.save(product);
        }
    }

    // Phương thức sẽ tự động chạy mỗi ngày vào lúc 00:00
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateVisibilityTypeForAllProducts() {
        Date now = new Date();

        List<Product> products = productRepository.findByIsDeletedFalse();

        for (Product product : products) {
            int totalSold = product.getTotalSold();
            updateVisibilityTypeBasedOnSales(product, totalSold, now);
        }
    }

    private void updateVisibilityTypeBasedOnSales(Product product, int totalSold, Date now) {
        if (totalSold >= 200) {
            product.setVisibilityType("Bán Chạy");
        }
        else if (totalSold >= 100) {
            product.setVisibilityType("Yêu Thích");
        }
        else if (totalSold >= 50) {
            product.setVisibilityType("Nổi Bật");
        }
        else if (totalSold >= 20) {
            product.setVisibilityType("Phổ Biến");
        }
        else if (totalSold < 20) {
            product.setVisibilityType("Mới");
        } else {
            product.setVisibilityType("Bình Thường");
        }

        product.setUpdatedAt(now);
        productRepository.save(product);
    }

    // Tạo một tên tệp duy nhất cho ảnh và lưu vào thư mục
    public String uploadImage(String productId, MultipartFile file) throws IOException {
        Path productImageDir = Paths.get(uploadDir + File.separator + productId);
        Files.createDirectories(productImageDir);

        // Tạo tên tệp duy nhất cho ảnh
        String imageFileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        Path path = Paths.get(productImageDir + File.separator + imageFileName);
        file.transferTo(path);

        return uploadUrl + "/" + productId + "/" + imageFileName;
    }

    // Xóa ảnh của sản phẩm
    public void deleteProductImage(String productId, String imageUrl) throws IOException {
        // Lấy sản phẩm từ database
        Optional<Product> optionalProduct = productRepository.findByIdAndIsDeletedFalse(productId);
        if (!optionalProduct.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId);
        }

        Product product = optionalProduct.get();

        // Kiểm tra và xóa ảnh trong danh sách images của sản phẩm
        List<String> images = product.getImages();
        if (!images.contains(imageUrl)) {
            throw new ResourceNotFoundException("Hình ảnh không tồn tại trong danh sách sản phẩm");
        }

        // Xóa ảnh khỏi danh sách
        images.remove(imageUrl);
        product.setImages(images);

        // Lưu lại sản phẩm sau khi xóa hình ảnh
        productRepository.save(product);

        // Xóa tệp ảnh khỏi hệ thống
        String imageFileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        Path imageFilePath = Paths.get(uploadDir + File.separator + productId + File.separator + imageFileName);
        File imageFile = imageFilePath.toFile();

        // Xóa tệp ảnh nếu tồn tại
        if (imageFile.exists() && !imageFile.delete()) {
            throw new IOException("Không thể xóa tệp ảnh: " + imageUrl);
        }
    }

    // Phương thức upload video cho sản phẩm
    public String uploadProductVideo(String productId, MultipartFile file) throws IOException {
        Path productVideoDir = Paths.get(uploadVideoDir + File.separator + productId);
        Files.createDirectories(productVideoDir);

        String videoFileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        Path path = Paths.get(productVideoDir + File.separator + videoFileName);
        file.transferTo(path);

        return uploadVideoUrl + "/" + productId + "/" + videoFileName;
    }

    // Phương thức xóa video của sản phẩm
    public void deleteProductVideo(String productId, String videoUrl) throws IOException {
        Optional<Product> optionalProduct = productRepository.findByIdAndIsDeletedFalse(productId);
        if (!optionalProduct.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId);
        }

        Product product = optionalProduct.get();

        List<String> videos = product.getVideos();
        if (videos.contains(videoUrl)) {
            videos.remove(videoUrl);
            product.setVideos(videos);
            productRepository.save(product);

            String videoFileName = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
            Path videoFilePath = Paths.get(uploadVideoDir + File.separator + productId + File.separator + videoFileName);
            File videoFile = videoFilePath.toFile();
            if (videoFile.exists() && !videoFile.delete()) {
                throw new IOException("Không thể xóa video: " + videoUrl);
            }
        } else {
            throw new ResourceNotFoundException("Video không tồn tại trong danh sách sản phẩm");
        }
    }

    // Chỉnh sửa sản phẩm
    public ProductDTO updateProduct(String id, ProductDTO productDTO) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setBrandId(productDTO.getBrandId() != null ? new ObjectId(productDTO.getBrandId()) : null);
            product.setCategoryId(productDTO.getCategoryId() != null ? new ObjectId(productDTO.getCategoryId()) : null);
            product.setPricing(convertPricingFromDTO(productDTO.getPricing()));
            product.setSpecifications(productDTO.getSpecifications());
            product.setVariants(productDTO.getVariants().stream().map(this::convertVariantFromDTO).collect(Collectors.toList()));
            product.setTags(productDTO.getTags());
            product.setImages(productDTO.getImages());
            product.setVideos(productDTO.getVideos());
            product.setBlog(convertBlogFromDTO(productDTO.getBlog()));
            product.setRatings(convertRatingsFromDTO(productDTO.getRatings()));
            product.setWarranty(convertWarrantyFromDTO(productDTO.getWarranty()));
            product.setTotalSold(productDTO.getTotalSold());
            product.setStatus(productDTO.getStatus());
            product.setVisibilityType(productDTO.getVisibilityType());
            product.setUpdatedAt(new Date());

            Product updatedProduct = productRepository.save(product);
            return convertToDTO(updatedProduct);
        } else {
            throw new ResourceNotFoundException("Sản phẩm không tồn tại.");
        }
    }

    // Cập nhật giá và lưu lại lịch sử giá
    public ProductDTO updateProductPrice(String productId, double newPrice) {
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isEmpty()) {
            throw new ResourceNotFoundException("Sản phẩm không tồn tại");
        }

        Product product = productOpt.get();
        double previousPrice = product.getPricing().getCurrent();
        product.getPricing().setCurrent(newPrice);

        // Lưu lại lịch sử thay đổi giá
        Product.Pricing.PriceHistory priceHistory = new Product.Pricing.PriceHistory();
        priceHistory.setPreviousPrice(previousPrice);
        priceHistory.setNewPrice(newPrice);
        priceHistory.setChangedAt(new Date());

        // Thêm lịch sử giá vào danh sách lịch sử giá
        if (product.getPricing().getHistory() == null) {
            product.getPricing().setHistory(new ArrayList<>());
        }
        product.getPricing().getHistory().add(priceHistory);

        // Cập nhật sản phẩm với giá mới và lịch sử
        product.setUpdatedAt(new Date());
        productRepository.save(product);
        return convertToDTO(product);
    }

    // Ẩn sản phẩm
    public void hideProduct(String id) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setStatus("inactive");
            product.setUpdatedAt(new Date());
            productRepository.save(product);
        } else {
            throw new ResourceNotFoundException("Sản phẩm không tồn tại.");
        }
    }

    // Hiện sản phẩm
    public void showProduct(String id) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setStatus("active");
            product.setUpdatedAt(new Date());
            productRepository.save(product);
        } else {
            throw new ResourceNotFoundException("Sản phẩm không tồn tại.");
        }
    }

    // Xóa sản phẩm
    public void deleteProduct(String id) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setDeleted(true);
            product.setUpdatedAt(new Date());
            productRepository.save(product);
        } else {
            throw new ResourceNotFoundException("Sản phẩm không tồn tại.");
        }
    }

    // Xuất tất cả sản phẩm ra file Excel
    public ByteArrayOutputStream exportProductsToExcel(String sortBy, String sortOrder) throws IOException {
        // Lấy tất cả sản phẩm từ repository (dữ liệu từ ProductDTO)
        List<ProductDTO> products = getAllInactiveProducts(sortBy, sortOrder);

        // Tạo workbook Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");

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
                "ID", "Mã sản phẩm", "Tên sản phẩm", "Mô tả", "Giá gốc", "Giá hiện tại",
                "Thương hiệu", "Danh mục", "Thông số kỹ thuật", "Biến thể", "Tags",
                "Videos", "Blog Tiêu đề", "Blog Mô tả", "Blog Nội dung", "Đánh giá trung bình",
                "Tổng số lượt đánh giá", "Bảo hành", "Tổng số lượng đã bán","Trạng thái", "Loại hiển thị", "Trạng thái xóa",
                "Thời gian lên lịch"
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

        // Thêm dữ liệu sản phẩm vào file Excel
        int rowNum = 1;
        for (ProductDTO product : products) {
            Row row = sheet.createRow(rowNum++);

            // Cột ID
            row.createCell(0).setCellValue(product.getId());
            row.getCell(0).setCellStyle(dataStyle);

            // Cột Mã sản phẩm
            row.createCell(1).setCellValue(product.getProductId());
            row.getCell(1).setCellStyle(dataStyle);

            // Cột Tên sản phẩm
            row.createCell(2).setCellValue(product.getName());
            row.getCell(2).setCellStyle(dataStyle);

            // Cột Mô tả
            row.createCell(3).setCellValue(product.getDescription());
            row.getCell(3).setCellStyle(dataStyle);

            // Cột Giá gốc
            row.createCell(4).setCellValue(product.getPricing().getOriginal());
            row.getCell(4).setCellStyle(dataStyle);

            // Cột Giá hiện tại
            row.createCell(5).setCellValue(product.getPricing().getCurrent());
            row.getCell(5).setCellStyle(dataStyle);

            // Cột Thương hiệu
            row.createCell(6).setCellValue(product.getBrandId());
            row.getCell(6).setCellStyle(dataStyle);

            // Cột Danh mục
            row.createCell(7).setCellValue(product.getCategoryId());
            row.getCell(7).setCellStyle(dataStyle);

            // Cột Thông số kỹ thuật
            row.createCell(8).setCellValue(product.getSpecifications().toString());
            row.getCell(8).setCellStyle(dataStyle);

            // Cột Biến thể
            row.createCell(9).setCellValue(product.getVariants().toString());
            row.getCell(9).setCellStyle(dataStyle);

            // Cột Tags
            row.createCell(10).setCellValue(String.join(", ", product.getTags()));
            row.getCell(10).setCellStyle(dataStyle);

            // Cột Videos
            row.createCell(11).setCellValue(String.join(", ", product.getVideos()));
            row.getCell(11).setCellStyle(dataStyle);

            // Cột Blog Tiêu đề
            row.createCell(12).setCellValue(product.getBlog() != null ? product.getBlog().getTitle() : "N/A");
            row.getCell(12).setCellStyle(dataStyle);

            // Cột Blog Mô tả
            row.createCell(13).setCellValue(product.getBlog() != null ? product.getBlog().getDescription() : "N/A");
            row.getCell(13).setCellStyle(dataStyle);

            // Cột Blog Nội dung
            row.createCell(14).setCellValue(product.getBlog() != null ? product.getBlog().getContent() : "N/A");
            row.getCell(14).setCellStyle(dataStyle);

            // Cột Đánh giá trung bình
            row.createCell(15).setCellValue(product.getRatings() != null ? product.getRatings().getAverage() : 0.0);
            row.getCell(15).setCellStyle(dataStyle);

            // Cột Tổng số lượt đánh giá
            row.createCell(16).setCellValue(product.getRatings() != null ? product.getRatings().getTotalReviews() : 0);
            row.getCell(16).setCellStyle(dataStyle);

            // Cột Bảo hành
            row.createCell(17).setCellValue(product.getWarranty() != null ? product.getWarranty().getDuration() : "N/A");
            row.getCell(17).setCellStyle(dataStyle);

            // Cột Tổng số lượng đã bán
            row.createCell(18).setCellValue(product.getTotalSold());
            row.getCell(18).setCellStyle(dataStyle);

            // Cột Trạng thái
            row.createCell(19).setCellValue(product.getStatus());
            row.getCell(19).setCellStyle(dataStyle);

            // Cột Loại hiển thị
            row.createCell(20).setCellValue(product.getVisibilityType());
            row.getCell(20).setCellStyle(dataStyle);

            // Cột Trạng thái xóa
            row.createCell(21).setCellValue(product.isDeleted() ? "Đã xóa" : "Đang hiển thị");
            row.getCell(21).setCellStyle(dataStyle);

            // Cột Thời gian lên lịch
            row.createCell(22).setCellValue(product.getScheduledDate() != null ? product.getScheduledDate().toString() : "N/A");
            row.getCell(22).setCellStyle(dataStyle);
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
}