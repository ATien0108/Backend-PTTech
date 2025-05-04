package com.hcmute.pttechecommercewebsite.controller;

import com.hcmute.pttechecommercewebsite.dto.ProductDTO;
import com.hcmute.pttechecommercewebsite.exception.ResourceNotFoundException;
import com.hcmute.pttechecommercewebsite.service.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // API lấy tất cả sản phẩm hoạt động
    @GetMapping("")
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam String sortBy, @RequestParam String sortOrder) {
        List<ProductDTO> products = productService.getAllProducts(sortBy, sortOrder);
        return ResponseEntity.ok(products);
    }

    // API lấy tất cả sản phẩm hoạt động
    @GetMapping("/active")
    public ResponseEntity<List<ProductDTO>> getActiveProducts(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) List<String> brandName,
            @RequestParam(required = false) List<String> categoryName,
            @RequestParam(required = false) List<String> visibilityType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        List<ProductDTO> result = productService.getAllActiveProducts(
                sortBy, sortOrder, brandName, categoryName, visibilityType, minPrice, maxPrice
        );
        return ResponseEntity.ok(result);
    }

    // API lấy tất cả sản phẩm không hoạt động
    @GetMapping("/inactive")
    public ResponseEntity<List<ProductDTO>> getAllInactiveProducts(
            @RequestParam String sortBy, @RequestParam String sortOrder) {
        List<ProductDTO> products = productService.getAllInactiveProducts(sortBy, sortOrder);
        return ResponseEntity.ok(products);
    }

    // API lấy 10 sản phẩm bán chạy nhất
    @GetMapping("/top-selling")
    public ResponseEntity<List<ProductDTO>> getTopSellingProducts() {
        List<ProductDTO> products = productService.getTopSellingProducts();
        return ResponseEntity.ok(products);
    }

    // API lấy 10 sản phẩm có số lượng đánh giá và số sao cao nhất
    @GetMapping("/top-rated")
    public ResponseEntity<List<ProductDTO>> getTopRatedProducts() {
        List<ProductDTO> products = productService.getTopRatedProducts();
        return ResponseEntity.ok(products);
    }

    // API lấy những sản phẩm có số lượng tồn kho dưới 10
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts() {
        List<ProductDTO> products = productService.getLowStockProducts();
        return ResponseEntity.ok(products);
    }

    // API tìm kiếm sản phẩm theo tên
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProductsByName(@RequestParam String keyword) {
        List<ProductDTO> products = productService.searchProductsByName(keyword);
        return ResponseEntity.ok(products);
    }

    // API lấy sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String id) {
        Optional<ProductDTO> productDTO = productService.getProductById(id);
        return productDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-product-id/{productId}")
    public ResponseEntity<ProductDTO> getProductByProductId(@PathVariable String productId) {
        Optional<ProductDTO> productDTO = productService.getProductByProductId(productId);
        return productDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // API tạo mới sản phẩm
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    // API cập nhật sản phẩm
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable String id, @RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    // API để cập nhật giá sản phẩm
    @PutMapping("/update-price/{productId}")
    public ResponseEntity<ProductDTO> updateProductPrice(
            @PathVariable String productId,
            @RequestParam double newPrice) {

        try {
            ProductDTO updatedProduct = productService.updateProductPrice(productId, newPrice);

            return ResponseEntity.ok(updatedProduct);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // API xóa sản phẩm (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // API ẩn sản phẩm
    @PutMapping("/hide/{id}")
    public ResponseEntity<Void> hideProduct(@PathVariable String id) {
        productService.hideProduct(id);
        return ResponseEntity.noContent().build();
    }

    // API hiện sản phẩm
    @PutMapping("/show/{id}")
    public ResponseEntity<Void> showProduct(@PathVariable String id) {
        productService.showProduct(id);
        return ResponseEntity.noContent().build();
    }

    // API tải ảnh sản phẩm lên
    @PostMapping("/upload-image/{productId}")
    public ResponseEntity<String> uploadImage(@PathVariable String productId, @RequestParam MultipartFile file) throws IOException {
        String imageUrl = productService.uploadImage(productId, file);
        return ResponseEntity.ok(imageUrl);
    }

    // API xóa ảnh sản phẩm
    @DeleteMapping("/delete-image/{productId}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable String productId, @RequestParam String imageUrl) throws IOException {
        productService.deleteProductImage(productId, imageUrl);
        return ResponseEntity.noContent().build();
    }

    // API upload video cho sản phẩm
    @PostMapping("/upload-video/{productId}")
    public ResponseEntity<String> uploadProductVideo(@PathVariable String productId, @RequestParam MultipartFile file) {
        try {
            String videoUrl = productService.uploadProductVideo(productId, file);
            return ResponseEntity.ok(videoUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi khi upload video: " + e.getMessage());
        }
    }

    // API xóa video của sản phẩm
    @DeleteMapping("/delete-video/{productId}")
    public ResponseEntity<String> deleteProductVideo(@PathVariable String productId, @RequestParam("videoUrl") String videoUrl) {
        try {
            productService.deleteProductVideo(productId, videoUrl);
            return ResponseEntity.ok("Video đã được xóa thành công");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi khi xóa video: " + e.getMessage());
        }
    }

    // API tạo sản phẩm với thời gian lên lịch
    @PostMapping("/schedule")
    public ResponseEntity<ProductDTO> scheduleCreateProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO scheduledProduct = productService.scheduleCreateProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduledProduct);
    }

    // API xuất danh sách sản phẩm ra file Excel
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportProductsToExcel(
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {

        try {
            ByteArrayOutputStream outputStream = productService.exportProductsToExcel(sortBy, sortOrder);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Lỗi khi xuất file Excel: " + e.getMessage()).getBytes());
        }
    }
}