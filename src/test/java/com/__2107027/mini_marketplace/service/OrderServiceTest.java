package com.__2107027.mini_marketplace.service;

import com.__2107027.mini_marketplace.dto.OrderItemRequest;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User buyer;
    private Product product;
    private Order pendingOrder;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("buyer", null, Collections.emptyList())
        );

        buyer = new User();
        buyer.setId(1L);
        buyer.setUsername("buyer");
        buyer.setRole("user");

        product = new Product("Gaming Laptop", "RTX 4060", new BigDecimal("75000.00"), 2L);
        product.setId(1L);
        product.setCreatedAt(LocalDateTime.now());

        pendingOrder = new Order(1L, "pending");
        pendingOrder.setId(1L);
        pendingOrder.setCreatedAt(LocalDateTime.now());

        orderItem = new OrderItem(1L, 1L, 2, new BigDecimal("75000.00"));
        orderItem.setId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── placeOrder ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("placeOrder: should create order and snapshot product price")
    void placeOrder_success_snapshotsProductPrice() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemRequest));

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(orderItem));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("pending");
        assertThat(response.getBuyerId()).isEqualTo(1L);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("150000.00"); // 75000 × 2
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
    }

    @Test
    @DisplayName("placeOrder: should throw ResourceNotFoundException when product not found")
    void placeOrder_productNotFound_throwsException() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(999L);
        itemRequest.setQuantity(1);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemRequest));

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999");

        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    // ── getMyOrders ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyOrders: should return only current user's orders")
    void getMyOrders_returnsCurrentUserOrders() {
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findByBuyerId(1L)).thenReturn(List.of(pendingOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(orderItem));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));

        List<OrderResponse> result = orderService.getMyOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBuyerUsername()).isEqualTo("buyer");
        verify(orderRepository, times(1)).findByBuyerId(1L);
    }

    // ── cancelOrder ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelOrder: should cancel a pending order owned by the user")
    void cancelOrder_pending_success() {
        Order cancelledOrder = new Order(1L, "cancelled");
        cancelledOrder.setId(1L);
        cancelledOrder.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(orderItem));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));

        OrderResponse result = orderService.cancelOrder(1L);

        assertThat(result.getStatus()).isEqualTo("cancelled");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("cancelOrder: should throw exception when order is already completed")
    void cancelOrder_alreadyCompleted_throwsIllegalArgument() {
        Order completedOrder = new Order(1L, "completed");
        completedOrder.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("pending");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("cancelOrder: should throw AccessDeniedException when not the owner")
    void cancelOrder_byNonOwner_throwsAccessDenied() {
        User otherBuyer = new User();
        otherBuyer.setId(99L);
        otherBuyer.setUsername("buyer");
        otherBuyer.setRole("user");

        // Order owned by buyer id=1, but current user id=99
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(otherBuyer));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
            .isInstanceOf(AccessDeniedException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    // ── getOrderById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getOrderById: should throw ResourceNotFoundException when not found")
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getOrderById: non-owner viewing another's order throws AccessDeniedException")
    void getOrderById_byNonOwner_throwsAccessDenied() {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setUsername("buyer");
        otherUser.setRole("user");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder)); // buyerId=1
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(otherUser)); // id=99

        assertThatThrownBy(() -> orderService.getOrderById(1L))
            .isInstanceOf(AccessDeniedException.class);
    }
}
