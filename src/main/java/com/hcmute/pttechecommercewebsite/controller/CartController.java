package com.hcmute.pttechecommercewebsite.controller;

import com.hcmute.pttechecommercewebsite.dto.CartDTO;
import com.hcmute.pttechecommercewebsite.service.CartService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    // API lấy tất cả giỏ hàng
    @GetMapping("")
    public List<CartDTO> getAllCarts() {
        return cartService.getAllCarts();
    }

    // Lấy giỏ hàng theo cartId
    @GetMapping("/{cartId}")
    public ResponseEntity<CartDTO> getCartById(@PathVariable String cartId) {
        CartDTO cartDTO = cartService.getCartById(cartId);
        return ResponseEntity.ok(cartDTO);
    }

    // Lấy giỏ hàng theo userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartDTO> getCartByUserId(@PathVariable String userId) {
        CartDTO cartDTO = cartService.getCartByUserId(new ObjectId(userId));
        return ResponseEntity.ok(cartDTO);
    }

    // Thêm giỏ hàng mới
    @PostMapping()
    public ResponseEntity<CartDTO> addCart(@RequestBody CartDTO cartDTO) {
        CartDTO savedCart = cartService.addCart(cartDTO);
        return ResponseEntity.ok(savedCart);
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartDTO> addItemToCart(@PathVariable String cartId, @RequestBody CartDTO.ItemDTO itemDTO) {
        CartDTO updatedCart = cartService.addItemToCart(cartId, itemDTO);
        return ResponseEntity.ok(updatedCart);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/{cartId}/items/{productId}/{variantId}")
    public ResponseEntity<CartDTO> removeItemFromCart(@PathVariable String cartId, @PathVariable String productId, @PathVariable String variantId) {
        CartDTO updatedCart = cartService.removeItemFromCart(cartId, productId, variantId);
        return ResponseEntity.ok(updatedCart);
    }

    // Tăng số lượng sản phẩm trong giỏ hàng
    @PutMapping("/{cartId}/increase/{productId}/{variantId}")
    public ResponseEntity<CartDTO> increaseItemQuantity(@PathVariable String cartId, @PathVariable String productId, @PathVariable String variantId) {
        CartDTO updatedCart = cartService.increaseItemQuantity(cartId, productId, variantId);
        return ResponseEntity.ok(updatedCart);
    }

    // Giảm số lượng sản phẩm trong giỏ hàng
    @PutMapping("/{cartId}/decrease/{productId}/{variantId}")
    public ResponseEntity<CartDTO> decreaseItemQuantity(@PathVariable String cartId, @PathVariable String productId, @PathVariable String variantId) {
        CartDTO updatedCart = cartService.decreaseItemQuantity(cartId, productId, variantId);
        return ResponseEntity.ok(updatedCart);
    }

    // Thay đổi biến thể sản phẩm trong giỏ hàng
    @PutMapping("/{cartId}/change-variant/{productId}/{oldVariantId}/{newVariantId}")
    public ResponseEntity<CartDTO> changeItemVariant(@PathVariable String cartId, @PathVariable String productId, @PathVariable String oldVariantId, @PathVariable String newVariantId) {
        CartDTO updatedCart = cartService.changeItemVariant(cartId, productId, oldVariantId, newVariantId);
        return ResponseEntity.ok(updatedCart);
    }

    // Xóa giỏ hàng (xóa mềm)
    @DeleteMapping("/{cartId}")
    public ResponseEntity<String> deleteCart(@PathVariable String cartId) {
        cartService.deleteCart(cartId);
        return ResponseEntity.ok("Giỏ hàng đã được xóa.");
    }
}