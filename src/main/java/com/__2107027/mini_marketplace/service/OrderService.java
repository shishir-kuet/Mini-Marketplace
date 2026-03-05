package com.__2107027.mini_marketplace.service;

import com.__2107027.mini_marketplace.dto.OrderItemRequest;
import com.__2107027.mini_marketplace.dto.OrderItemResponse;
import com.__2107027.mini_marketplace.dto.OrderRequest;
import com.__2107027.mini_marketplace.dto.OrderResponse;
import com.__2107027.mini_marketplace.dto.OrderStatusRequest;
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
import java.util.Set;
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

            if (product.getStockCount() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException(
                        "Not enough stock for '" + product.getTitle() + "'. Available: " + product.getStockCount());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice()); // snapshot price at time of purchase
            orderItemRepository.save(orderItem);

            // Decrement stock
            product.setStockCount(product.getStockCount() - itemRequest.getQuantity());
            productRepository.save(product);
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

        // Restore stock for each item
        orderItemRepository.findByOrderId(order.getId()).forEach(item ->
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setStockCount(product.getStockCount() + item.getQuantity());
                productRepository.save(product);
            })
        );

        order.setStatus("cancelled");
        return toResponse(orderRepository.save(order));
    }

    // ── Seller actions ─────────────────────────────────────────────────────────

    /**
     * Returns all orders that contain at least one product owned by the current seller.
     */
    public List<OrderResponse> getSellerOrders() {
        User currentUser = getCurrentUser();

        // Collect all product IDs that belong to this seller
        Set<Long> sellerProductIds = productRepository.findBySellerId(currentUser.getId())
                .stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        if (sellerProductIds.isEmpty()) {
            return List.of();
        }

        // Find all order items that reference any of these products
        Set<Long> orderIds = sellerProductIds.stream()
                .flatMap(pid -> orderItemRepository.findByProductId(pid).stream())
                .map(item -> item.getOrderId())
                .collect(Collectors.toSet());

        if (orderIds.isEmpty()) {
            return List.of();
        }

        return orderRepository.findByIdIn(orderIds)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Seller can update status to: processing, shipped, delivered.
     * Admin can update to any valid status.
     * Seller must own at least one product in the order.
     */
    public OrderResponse updateOrderStatus(Long id, OrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        User currentUser = getCurrentUser();
        String newStatus = request.getStatus();

        if (currentUser.isAdmin()) {
            throw new AccessDeniedException("Admins cannot update order status. Only the seller can.");
        }

        // Must be seller of at least one product in this order
        Set<Long> sellerProductIds = productRepository.findBySellerId(currentUser.getId())
                .stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        boolean isSellerOfOrder = orderItemRepository.findByOrderId(order.getId())
                .stream()
                .anyMatch(item -> sellerProductIds.contains(item.getProductId()));

        if (!isSellerOfOrder) {
            throw new AccessDeniedException("You can only update status of orders containing your products");
        }

        // Seller can only move to these statuses
        if (!List.of("processing", "shipped", "delivered", "cancelled").contains(newStatus)) {
            throw new AccessDeniedException("Sellers can only set status to: processing, shipped, delivered, cancelled");
        }

        if ("cancelled".equals(order.getStatus()) || "completed".equals(order.getStatus())) {
            throw new IllegalArgumentException("Cannot update a " + order.getStatus() + " order");
        }

        // Restore stock when cancelling
        if ("cancelled".equals(newStatus)) {
            orderItemRepository.findByOrderId(order.getId()).forEach(item ->
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setStockCount(product.getStockCount() + item.getQuantity());
                    productRepository.save(product);
                })
            );
        }

        order.setStatus(newStatus);
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
