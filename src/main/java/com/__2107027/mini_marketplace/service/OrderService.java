package com.__2107027.mini_marketplace.service;

import com.__2107027.mini_marketplace.dto.OrderItemRequest;
import com.__2107027.mini_marketplace.dto.OrderItemResponse;
import com.__2107027.mini_marketplace.dto.OrderRequest;
import com.__2107027.mini_marketplace.dto.OrderResponse;
import com.__2107027.mini_marketplace.exception.ResourceNotFoundException;
import com.__2107027.mini_marketplace.model.Order;
import com.__2107027.mini_marketplace.model.OrderItem;
import com.__2107027.mini_marketplace.model.Product;
import com.__2107027.mini_marketplace.model.User;
import com.__2107027.mini_marketplace.repository.OrderItemRepository;
import com.__2107027.mini_marketplace.repository.OrderRepository;
import com.__2107027.mini_marketplace.repository.ProductRepository;
import com.__2107027.mini_marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        List<OrderItemResponse> itemResponses = items.stream().map(item -> {
            String productTitle = productRepository.findById(item.getProductId())
                    .map(Product::getTitle)
                    .orElse("Deleted product");
            return new OrderItemResponse(
                    item.getId(),
                    item.getProductId(),
                    productTitle,
                    item.getQuantity(),
                    item.getPrice()
            );
        }).collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(OrderItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String buyerUsername = userRepository.findById(order.getBuyerId())
                .map(User::getUsername)
                .orElse("unknown");

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setBuyerId(order.getBuyerId());
        response.setBuyerUsername(buyerUsername);
        response.setStatus(order.getStatus());
        response.setItems(itemResponses);
        response.setTotalAmount(total);
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }

    // ── Place order ───────────────────────────────────────────────────────────

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        User currentUser = getCurrentUser();

        // Create the order
        Order order = new Order(currentUser.getId(), "pending");
        order = orderRepository.save(order);

        // Create order items using current product prices
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice()); // snapshot price at time of purchase
            orderItemRepository.save(orderItem);
        }

        return toResponse(order);
    }

    // ── My orders ─────────────────────────────────────────────────────────────

    public List<OrderResponse> getMyOrders() {
        User currentUser = getCurrentUser();
        return orderRepository.findByBuyerId(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        User currentUser = getCurrentUser();
        boolean isOwner = order.getBuyerId().equals(currentUser.getId());
        boolean isAdmin = currentUser.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only view your own orders");
        }

        return toResponse(order);
    }

    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!order.getBuyerId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new AccessDeniedException("You can only cancel your own orders");
        }

        if (!"pending".equals(order.getStatus())) {
            throw new IllegalArgumentException("Only pending orders can be cancelled");
        }

        order.setStatus("cancelled");
        return toResponse(orderRepository.save(order));
    }

    // ── Admin actions ─────────────────────────────────────────────────────────

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse completeOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (!"pending".equals(order.getStatus())) {
            throw new IllegalArgumentException("Only pending orders can be completed");
        }

        order.setStatus("completed");
        return toResponse(orderRepository.save(order));
    }
}
