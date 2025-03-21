package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.InventoryDTO;
import com.hcmute.pttechecommercewebsite.model.Inventory;
import com.hcmute.pttechecommercewebsite.model.Product;
import com.hcmute.pttechecommercewebsite.repository.InventoryRepository;
import com.hcmute.pttechecommercewebsite.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // Trong InventoryService, sửa phương thức getAllInventories
    public List<InventoryDTO> getAllInventories(String sortOrder) {
        Sort sort = Sort.by(Sort.Order.desc("receivedDate"));
        if ("asc".equals(sortOrder)) {
            sort = Sort.by(Sort.Order.asc("receivedDate"));
        }

        List<Inventory> inventories = inventoryRepository.findByIsDeletedFalse(sort);
        return inventories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lọc nhập kho theo Product ID
    public List<InventoryDTO> getInventoriesByProductId(String productId) {
        ObjectId objectId = new ObjectId(productId);
        List<Inventory> inventories = inventoryRepository.findByIsDeletedFalseAndProducts_ProductIdIn(List.of(objectId));
        return inventories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lọc nhập kho theo tổng số tiền (totalAmount) theo thứ tự tăng dần hoặc giảm dần
    public List<InventoryDTO> getInventoriesSortedByTotalAmount(boolean ascending) {
        Sort sort = ascending ? Sort.by(Sort.Order.asc("totalAmount")) : Sort.by(Sort.Order.desc("totalAmount"));
        List<Inventory> inventories = inventoryRepository.findByIsDeletedFalse(sort);
        return inventories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lọc nhập kho theo tổng số lượng (totalQuantity) theo thứ tự tăng dần hoặc giảm dần
    public List<InventoryDTO> getInventoriesSortedByTotalQuantity(boolean ascending) {
        Sort sort = ascending ? Sort.by(Sort.Order.asc("totalQuantity")) : Sort.by(Sort.Order.desc("totalQuantity"));
        List<Inventory> inventories = inventoryRepository.findByIsDeletedFalse(sort);
        return inventories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy thông tin nhập kho theo ID
    public Optional<InventoryDTO> getInventoryById(String id) {
        Optional<Inventory> inventory = inventoryRepository.findById(id);
        return inventory.map(this::convertToDTO);
    }

    // Tạo nhập kho mới
    public InventoryDTO createInventory(InventoryDTO inventoryDTO) {
        double totalAmount = 0;
        int totalQuantity = 0;

        for (InventoryDTO.ProductEntryDTO productEntryDTO : inventoryDTO.getProducts()) {
            // Truy vấn sản phẩm dựa trên productId
            Optional<Product> productOptional = productRepository.findByIdAndIsDeletedFalse(productEntryDTO.getProductId());
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                productEntryDTO.setProductName(product.getName());

                // Duyệt qua các biến thể của sản phẩm
                for (InventoryDTO.ProductEntryDTO.ProductVariantEntryDTO variantEntryDTO : productEntryDTO.getProductVariants()) {
                    product.getVariants().stream()
                            .filter(variant -> variant.getVariantId().toString().equals(variantEntryDTO.getProductVariantId()))
                            .findFirst()
                            .ifPresent(variant -> {
                                variantEntryDTO.setColor(variant.getColor());
                                variantEntryDTO.setSize(variant.getSize());
                                variantEntryDTO.setRam(variant.getRam());
                                variantEntryDTO.setStorage(variant.getStorage());
                                variantEntryDTO.setStockBeforeUpdate(variant.getStock());
                                variantEntryDTO.setStockAfterUpdate(variant.getStock() + variantEntryDTO.getQuantity());

                                // Cập nhật số lượng tồn kho cho biến thể trong sản phẩm
                                variant.setStock(variant.getStock() + variantEntryDTO.getQuantity());
                                productRepository.save(product);
                            });
                }
            }

            // Tính toán tổng giá trị và số lượng
            for (InventoryDTO.ProductEntryDTO.ProductVariantEntryDTO variantEntryDTO : productEntryDTO.getProductVariants()) {
                totalAmount += variantEntryDTO.getTotalValue();
                totalQuantity += variantEntryDTO.getQuantity();
            }
        }

        Date receivedDate = new Date();

        Inventory inventory = convertToModel(inventoryDTO, totalAmount, totalQuantity, receivedDate);
        Inventory savedInventory = inventoryRepository.save(inventory);
        return convertToDTO(savedInventory);
    }

    // Xóa nhập kho
    public boolean deleteInventory(String id) {
        Optional<Inventory> inventoryOptional = inventoryRepository.findById(id);
        if (inventoryOptional.isPresent()) {
            // Lấy thông tin nhập kho cần xóa
            Inventory inventory = inventoryOptional.get();

            // Nếu bản ghi đã bị xóa rồi, không thực hiện gì
            if (inventory.isDeleted()) {
                return false;
            }

            // Đánh dấu nhập kho là đã xóa (xóa mềm)
            inventory.setDeleted(true);

            // Duyệt qua các sản phẩm và biến thể để khôi phục lại số lượng tồn kho
            for (Inventory.ProductEntry productEntry : inventory.getProducts()) {
                // Chuyển ObjectId sang String nếu phương thức yêu cầu String
                String productId = productEntry.getProductId().toString();

                Optional<Product> productOptional = productRepository.findByIdAndIsDeletedFalse(productId);
                if (productOptional.isPresent()) {
                    Product product = productOptional.get();

                    // Duyệt qua từng biến thể và khôi phục lại số lượng tồn kho
                    for (Inventory.ProductEntry.ProductVariantEntry variantEntry : productEntry.getProductVariants()) {
                        product.getVariants().stream()
                                .filter(variant -> variant.getVariantId().toString().equals(variantEntry.getProductVariantId().toString()))
                                .findFirst()
                                .ifPresent(variant -> {
                                    int stockChange = variant.getStock() - variantEntry.getStockBeforeUpdate();
                                    variant.setStock(variant.getStock() - stockChange);
                                    productRepository.save(product);
                                });
                    }
                }
            }

            // Cập nhật lại nhập kho đã bị đánh dấu là xóa
            inventoryRepository.save(inventory);
            return true;
        }
        return false;
    }

    public ByteArrayOutputStream exportInventoriesToExcel(String sort) throws IOException {
        List<InventoryDTO> inventories = getAllInventories(sort);

        // Tạo workbook Excel
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inventories");

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
                "ID", "Tên nhà cung cấp", "Tổng giá trị", "Tổng số lượng", "Ngày nhập kho", "Ghi chú", "Trạng thái xóa",
                "ID Sản phẩm", "Tên sản phẩm", "Màu sắc", "Kích thước", "RAM", "Storage", "Số lượng", "Đơn giá", "Giá trị tổng",
                "Tồn kho trước", "Tồn kho sau"
        };

        // Thêm các cột tiêu đề
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Định dạng cho dữ liệu
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Thêm dữ liệu vào Excel
        int rowNum = 1;
        for (InventoryDTO inventory : inventories) {
            // Đối với mỗi inventory, tạo một dòng dữ liệu chính
            for (InventoryDTO.ProductEntryDTO productEntry : inventory.getProducts()) {
                for (InventoryDTO.ProductEntryDTO.ProductVariantEntryDTO variant : productEntry.getProductVariants()) {
                    Row row = sheet.createRow(rowNum++);

                    // Cột ID
                    row.createCell(0).setCellValue(inventory.getId());
                    row.getCell(0).setCellStyle(dataStyle);

                    // Cột Tên nhà cung cấp
                    row.createCell(1).setCellValue(inventory.getSupplier() != null ? inventory.getSupplier().getName() : "N/A");
                    row.getCell(1).setCellStyle(dataStyle);

                    // Cột Tổng giá trị
                    row.createCell(2).setCellValue(inventory.getTotalAmount());
                    row.getCell(2).setCellStyle(dataStyle);

                    // Cột Tổng số lượng
                    row.createCell(3).setCellValue(inventory.getTotalQuantity());
                    row.getCell(3).setCellStyle(dataStyle);

                    // Cột Ngày nhập kho
                    row.createCell(4).setCellValue(inventory.getReceivedDate() != null ? inventory.getReceivedDate().toString() : "N/A");
                    row.getCell(4).setCellStyle(dataStyle);

                    // Cột Ghi chú
                    row.createCell(5).setCellValue(inventory.getNotes() != null ? inventory.getNotes() : "N/A");
                    row.getCell(5).setCellStyle(dataStyle);

                    // Cột Trạng thái xóa
                    row.createCell(6).setCellValue(inventory.isDeleted() ? "Đã xóa" : "Chưa xóa");
                    row.getCell(6).setCellStyle(dataStyle);

                    // Cột ID sản phẩm
                    row.createCell(7).setCellValue(productEntry.getProductId());
                    row.getCell(7).setCellStyle(dataStyle);

                    // Cột Tên sản phẩm
                    row.createCell(8).setCellValue(productEntry.getProductName());
                    row.getCell(8).setCellStyle(dataStyle);

                    // Cột Màu sắc
                    row.createCell(9).setCellValue(variant.getColor() != null ? variant.getColor() : "N/A");
                    row.getCell(9).setCellStyle(dataStyle);

                    // Cột Kích thước
                    row.createCell(10).setCellValue(variant.getSize() != null ? variant.getSize() : "N/A");
                    row.getCell(10).setCellStyle(dataStyle);

                    // Cột RAM
                    row.createCell(11).setCellValue(variant.getRam() != null ? variant.getRam() : "N/A");
                    row.getCell(11).setCellStyle(dataStyle);

                    // Cột Storage
                    row.createCell(12).setCellValue(variant.getStorage() != null ? variant.getStorage() : "N/A");
                    row.getCell(12).setCellStyle(dataStyle);

                    // Cột Số lượng
                    row.createCell(13).setCellValue(variant.getQuantity());
                    row.getCell(13).setCellStyle(dataStyle);

                    // Cột Đơn giá
                    row.createCell(14).setCellValue(variant.getUnitPrice());
                    row.getCell(14).setCellStyle(dataStyle);

                    // Cột Giá trị tổng
                    row.createCell(15).setCellValue(variant.getTotalValue());
                    row.getCell(15).setCellStyle(dataStyle);

                    // Cột Tồn kho trước
                    row.createCell(16).setCellValue(variant.getStockBeforeUpdate());
                    row.getCell(16).setCellStyle(dataStyle);

                    // Cột Tồn kho sau
                    row.createCell(17).setCellValue(variant.getStockAfterUpdate());
                    row.getCell(17).setCellStyle(dataStyle);
                }
            }
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

    // Chuyển đổi từ Inventory model sang DTO
    private InventoryDTO convertToDTO(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .products(inventory.getProducts().stream()
                        .map(this::convertProductEntryToDTO)
                        .collect(Collectors.toList()))
                .supplier(convertSupplierToDTO(inventory.getSupplier()))
                .totalAmount(inventory.getTotalAmount())
                .totalQuantity(inventory.getTotalQuantity())
                .notes(inventory.getNotes())
                .receivedDate(inventory.getReceivedDate())
                .build();
    }

    // Chuyển đổi từ ProductEntry sang ProductEntryDTO
    private InventoryDTO.ProductEntryDTO convertProductEntryToDTO(Inventory.ProductEntry productEntry) {
        return InventoryDTO.ProductEntryDTO.builder()
                .productId(productEntry.getProductId().toString())
                .productName(productEntry.getProductName())
                .productVariants(productEntry.getProductVariants().stream()
                        .map(this::convertProductVariantEntryToDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    // Chuyển đổi từ ProductVariantEntry sang ProductVariantEntryDTO
    private InventoryDTO.ProductEntryDTO.ProductVariantEntryDTO convertProductVariantEntryToDTO(Inventory.ProductEntry.ProductVariantEntry productVariantEntry) {
        return InventoryDTO.ProductEntryDTO.ProductVariantEntryDTO.builder()
                .productVariantId(productVariantEntry.getProductVariantId().toString())
                .color(productVariantEntry.getColor())
                .size(productVariantEntry.getSize())
                .ram(productVariantEntry.getRam())
                .storage(productVariantEntry.getStorage())
                .quantity(productVariantEntry.getQuantity())
                .unitPrice(productVariantEntry.getUnitPrice())
                .totalValue(productVariantEntry.getTotalValue())
                .stockBeforeUpdate(productVariantEntry.getStockBeforeUpdate())
                .stockAfterUpdate(productVariantEntry.getStockAfterUpdate())
                .build();
    }

    // Chuyển đổi từ Supplier sang SupplierDTO
    private InventoryDTO.SupplierDTO convertSupplierToDTO(Inventory.Supplier supplier) {
        return InventoryDTO.SupplierDTO.builder()
                .name(supplier.getName())
                .contact(supplier.getContact())
                .address(supplier.getAddress())
                .build();
    }

    // Chuyển đổi từ InventoryDTO sang Inventory model
    private Inventory convertToModel(InventoryDTO inventoryDTO, double totalAmount, int totalQuantity, Date receivedDate) {
        Inventory.Supplier supplier = convertSupplierToModel(inventoryDTO.getSupplier());
        List<Inventory.ProductEntry> productEntries = inventoryDTO.getProducts().stream()
                .map(this::convertProductEntryToModel)
                .collect(Collectors.toList());

        return Inventory.builder()
                .products(productEntries)
                .supplier(supplier)
                .totalAmount(totalAmount)
                .totalQuantity(totalQuantity)
                .notes(inventoryDTO.getNotes())
                .receivedDate(receivedDate)
                .build();
    }

    // Chuyển đổi từ SupplierDTO sang Supplier
    private Inventory.Supplier convertSupplierToModel(InventoryDTO.SupplierDTO supplierDTO) {
        return new Inventory.Supplier(supplierDTO.getName(), supplierDTO.getContact(), supplierDTO.getAddress());
    }

    // Chuyển đổi từ ProductEntryDTO sang ProductEntry
    private Inventory.ProductEntry convertProductEntryToModel(InventoryDTO.ProductEntryDTO productEntryDTO) {
        List<Inventory.ProductEntry.ProductVariantEntry> productVariants = productEntryDTO.getProductVariants().stream()
                .map(this::convertProductVariantEntryToModel)
                .collect(Collectors.toList());

        // Chuyển từ String về ObjectId
        return new Inventory.ProductEntry(
                new ObjectId(productEntryDTO.getProductId()),  // Chuyển String thành ObjectId
                productEntryDTO.getProductName(),
                productVariants
        );
    }

    // Chuyển đổi từ ProductVariantEntryDTO sang ProductVariantEntry
    private Inventory.ProductEntry.ProductVariantEntry convertProductVariantEntryToModel(InventoryDTO.ProductEntryDTO.ProductVariantEntryDTO productVariantEntryDTO) {
        return new Inventory.ProductEntry.ProductVariantEntry(
                new ObjectId(productVariantEntryDTO.getProductVariantId()), // Chuyển String thành ObjectId
                productVariantEntryDTO.getColor(),
                productVariantEntryDTO.getSize(),
                productVariantEntryDTO.getRam(),
                productVariantEntryDTO.getStorage(),
                productVariantEntryDTO.getQuantity(),
                productVariantEntryDTO.getUnitPrice(),
                productVariantEntryDTO.getTotalValue(),
                productVariantEntryDTO.getStockBeforeUpdate(),
                productVariantEntryDTO.getStockAfterUpdate()
        );
    }
}