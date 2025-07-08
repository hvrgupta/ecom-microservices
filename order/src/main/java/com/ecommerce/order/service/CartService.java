package com.ecommerce.order.service;

import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.repository.CartItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartItemRepository cartItemRepository;

    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "addToCartFallback")
    public boolean addToCart(String userId, CartItemRequest request) {
        // Look for product
        ProductResponse productResponse = productServiceClient.getProduct(request.getProductId());
        if (productResponse == null || productResponse.getStockQuantity() < request.getQuantity())
            return false;

        UserResponse userResponse = userServiceClient.getUserDetails(String.valueOf(userId));

        if (userResponse == null)
            return false;

        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
        if (existingCartItem != null) {
            // Update the quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
            existingCartItem.setPrice(BigDecimal.valueOf(1000.00));
            cartItemRepository.save(existingCartItem);
        } else {
            // Create new cart item
           CartItem cartItem = new CartItem();
           cartItem.setUserId(userId);
           cartItem.setProductId(request.getProductId());
           cartItem.setQuantity(request.getQuantity());
           cartItem.setPrice(BigDecimal.valueOf(1000.00));
           cartItemRepository.save(cartItem);
        }
        return true;
    }

// Method parameter should be same as the protecting method
    public boolean addToCartFallback(String userId, CartItemRequest request, Exception exception) {
        exception.printStackTrace();
        return false;
    }

    public boolean deleteItemFromCart(String userId, Long productId) {

        if (cartItemRepository.findByUserIdAndProductId(userId, productId) != null) {
            cartItemRepository.deleteByUserIdAndProductId(userId, productId);
            return true;
        }
        return false;
    }

    public List<CartItem> getCart(String userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
