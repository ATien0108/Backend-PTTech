package com.hcmute.pttechecommercewebsite.service;

import com.hcmute.pttechecommercewebsite.dto.CartDTO;
import com.hcmute.pttechecommercewebsite.model.Cart;
import com.hcmute.pttechecommercewebsite.repository.CartRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    // Chuyển đổi từ CartDTO sang Cart (Model)
    private Cart toModel(CartDTO cartDTO) {
        Cart cart = Cart.builder()
                .id(cartDTO.getId())
                .userId(new ObjectId(cartDTO.getUserId()))
                .totalItems(cartDTO.getItems() != null ? cartDTO.getItems().size() : 0)
                .totalPrice(cartDTO.getItems() != null ? cartDTO.getItems().stream().mapToDouble(CartDTO.ItemDTO::getTotalPrice).sum() : 0.0)
                .isDeleted(false)
                .items(cartDTO.getItems() != null ? cartDTO.getItems().stream().map(this::toModelItem).toList() : new ArrayList<>())
                .build();
        return cart;
    }

    // Chuyển đổi từ ItemDTO sang Item (Model)
    private Cart.Item toModelItem(CartDTO.ItemDTO itemDTO) {
        Cart.Item item = Cart.Item.builder()
                .productId(new ObjectId(itemDTO.getProductId()))
                .variantId(new ObjectId(itemDTO.getVariantId()))
                .brandId(new ObjectId(itemDTO.getBrandId()))
                .categoryId(new ObjectId(itemDTO.getCategoryId()))
                .quantity(itemDTO.getQuantity())
                .productName(itemDTO.getProductName())
                .originalPrice(itemDTO.getOriginalPrice())
                .discountPrice(itemDTO.getDiscountPrice())
                .ratingAverage(itemDTO.getRatingAverage())
                .totalReviews(itemDTO.getTotalReviews())
                .productImage(itemDTO.getProductImage())
                .color(itemDTO.getColor())
                .hexCode(itemDTO.getHexCode())
                .size(itemDTO.getSize())
                .ram(itemDTO.getRam())
                .storage(itemDTO.getStorage())
                .condition(itemDTO.getCondition())
                .createdAt(itemDTO.getCreatedAt())
                .updatedAt(itemDTO.getUpdatedAt())
                .build();
        // Tính lại tổng giá trị ngay khi chuyển đổi
        item.setTotalPrice(item.getQuantity() * item.getDiscountPrice());
        return item;
    }

    // Chuyển đổi từ Cart (Model) sang CartDTO
    public CartDTO toDTO(Cart cart) {
        CartDTO cartDTO = CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId().toString())
                .totalItems(cart.getTotalItems())
                .totalPrice(cart.getTotalPrice())
                .isDeleted(cart.isDeleted())
                .items(cart.getItems().stream()
                        .map(this::toDTOItem)
                        .toList())
                .build();
        return cartDTO;
    }

    // Chuyển đổi từ Item (Model) sang ItemDTO
    private CartDTO.ItemDTO toDTOItem(Cart.Item item) {
        return CartDTO.ItemDTO.builder()
                .productId(item.getProductId().toString())
                .variantId(item.getVariantId().toString())
                .brandId(item.getBrandId().toString())
                .categoryId(item.getCategoryId().toString())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .productName(item.getProductName())
                .originalPrice(item.getOriginalPrice())
                .discountPrice(item.getDiscountPrice())
                .ratingAverage(item.getRatingAverage())
                .totalReviews(item.getTotalReviews())
                .productImage(item.getProductImage())
                .color(item.getColor())
                .hexCode(item.getHexCode())
                .size(item.getSize())
                .ram(item.getRam())
                .storage(item.getStorage())
                .condition(item.getCondition())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    // Lấy tất cả giỏ hàng
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();  // Lấy tất cả các giỏ hàng
        return carts.stream()
                .filter(cart -> !cart.isDeleted())  // Chỉ lấy giỏ hàng không bị xóa
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy thông tin giỏ hàng theo id
    public CartDTO getCartById(String cartId) {
        Optional<Cart> cartOpt = cartRepository.findByIdAndIsDeletedFalse(cartId);
        if (cartOpt.isPresent()) {
            return toDTO(cartOpt.get());
        } else {
            throw new RuntimeException("Giỏ hàng không tồn tại hoặc đã bị xóa.");
        }
    }

    // Lấy giỏ hàng của người dùng
    public CartDTO getCartByUserId(ObjectId userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserIdAndIsDeletedFalse(userId);
        if (cartOpt.isPresent()) {
            return toDTO(cartOpt.get());
        } else {
            throw new RuntimeException("Giỏ hàng của người dùng không tồn tại hoặc đã bị xóa.");
        }
    }

    // Thêm giỏ hàng mới
    public CartDTO addCart(CartDTO cartDTO) {
        Cart cart = toModel(cartDTO);
        Cart savedCart = cartRepository.save(cart);
        return toDTO(savedCart);
    }

    // Thêm sản phẩm vào giỏ hàng
    public CartDTO addItemToCart(String cartId, CartDTO.ItemDTO itemDTO) {
        Optional<Cart> cartOpt = cartRepository.findByIdAndIsDeletedFalse(cartId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();

            // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
            Cart.Item existingItem = findItem(cart, itemDTO.getProductId(), itemDTO.getVariantId());
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + itemDTO.getQuantity());
                existingItem.setTotalPrice(existingItem.getQuantity() * existingItem.getDiscountPrice());
            } else {
                Cart.Item newItem = toModelItem(itemDTO);
                newItem.setTotalPrice(newItem.getQuantity() * newItem.getDiscountPrice());
                cart.getItems().add(newItem);
            }

            // Cập nhật tổng số sản phẩm và tổng giá trị của giỏ hàng
            cart.setTotalItems(cart.getItems().size());
            cart.setTotalPrice(cart.getItems().stream().mapToDouble(Cart.Item::getTotalPrice).sum());

            try {
                Cart updatedCart = cartRepository.save(cart);
                return toDTO(updatedCart);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi lưu giỏ hàng vào cơ sở dữ liệu: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Giỏ hàng không tồn tại hoặc đã bị xóa.");
        }
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public CartDTO removeItemFromCart(String cartId, String productId, String variantId) {
        Optional<Cart> cartOpt = cartRepository.findByIdAndIsDeletedFalse(cartId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            Cart.Item itemToRemove = findItem(cart, productId, variantId);
            if (itemToRemove != null) {
                cart.getItems().remove(itemToRemove);

                // Cập nhật tổng số sản phẩm và tổng giá trị của giỏ hàng
                cart.setTotalItems(cart.getItems().size());
                cart.setTotalPrice(cart.getItems().stream().mapToDouble(Cart.Item::getTotalPrice).sum());

                Cart updatedCart = cartRepository.save(cart);
                return toDTO(updatedCart);
            } else {
                throw new RuntimeException("Sản phẩm không tồn tại trong giỏ hàng.");
            }
        } else {
            throw new RuntimeException("Giỏ hàng không tồn tại hoặc đã bị xóa.");
        }
    }

    // Tăng số lượng sản phẩm trong giỏ hàng
    public CartDTO increaseItemQuantity(String cartId, String productId, String variantId) {
        Optional<Cart> cartOpt = cartRepository.findByIdAndIsDeletedFalse(cartId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            Cart.Item item = findItem(cart, productId, variantId);

            // Tăng số lượng và tính lại tổng giá trị
            item.setQuantity(item.getQuantity() + 1);
            item.setTotalPrice(item.getQuantity() * item.getDiscountPrice());

            // Cập nhật lại tổng số sản phẩm và tổng giá trị của giỏ hàng
            cart.setTotalItems(cart.getItems().size());
            cart.setTotalPrice(cart.getItems().stream().mapToDouble(Cart.Item::getTotalPrice).sum());

            cartRepository.save(cart);
            return toDTO(cart);
        } else {
            throw new RuntimeException("Giỏ hàng không tồn tại hoặc đã bị xóa.");
        }
    }

    // Giảm số lượng sản phẩm trong giỏ hàng
    public CartDTO decreaseItemQuantity(String cartId, String productId, String variantId) {
        Optional<Cart> cartOpt = cartRepository.findByIdAndIsDeletedFalse(cartId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            Cart.Item item = findItem(cart, productId, variantId);

            // Giảm số lượng và tính lại tổng giá trị
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                item.setTotalPrice(item.getQuantity() * item.getDiscountPrice());
            }

            // Cập nhật lại tổng số sản phẩm và tổng giá trị của giỏ hàng
            cart.setTotalItems(cart.getItems().size());
            cart.setTotalPrice(cart.getItems().stream().mapToDouble(Cart.Item::getTotalPrice).sum());

            cartRepository.save(cart);
            return toDTO(cart);
        } else {
            throw new RuntimeException("Giỏ hàng không tồn tại hoặc đã bị xóa.");
        }
    }

    private Cart.Item findItem(Cart cart, String productId, String variantId) {
        return cart.getItems().stream()
                .filter(item -> item.getProductId().toString().equals(productId) && item.getVariantId().toString().equals(variantId))
                .findFirst()
                .orElse(null);
    }

    // Thay đổi biến thể sản phẩm trong giỏ hàng
    public CartDTO changeItemVariant(String cartId, String productId, String oldVariantId, String newVariantId) {
        Optional<Cart> cartOpt = cartRepository.findByIdAndIsDeletedFalse(cartId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            Cart.Item item = findItem(cart, productId, oldVariantId);
            item.setVariantId(new ObjectId(newVariantId));
            cartRepository.save(cart);
            return toDTO(cart);
        } else {
            throw new RuntimeException("Giỏ hàng không tồn tại hoặc đã bị xóa.");
        }
    }

    // Xóa giỏ hàng (xóa mềm)
    public void deleteCart(String cartId) {
        Optional<Cart> cartOpt = cartRepository.findById(cartId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.setDeleted(true);
            cartRepository.save(cart);
        } else {
            throw new RuntimeException("Giỏ hàng không tồn tại.");
        }
    }
}
