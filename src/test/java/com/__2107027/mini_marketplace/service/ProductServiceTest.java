package com.__2107027.mini_marketplace.service;

import com.__2107027.mini_marketplace.dto.ProductRequest;
import com.__2107027.mini_marketplace.dto.ProductResponse;
import com.__2107027.mini_marketplace.exception.ResourceNotFoundException;
import com.__2107027.mini_marketplace.model.Product;
import com.__2107027.mini_marketplace.model.User;
import com.__2107027.mini_marketplace.repository.ProductRepository;
import com.__2107027.mini_marketplace.repository.ReviewRepository;
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
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ProductService productService;

    private User seller;
    private User adminUser;
    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        // Set authenticated user in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("testuser", null, Collections.emptyList())
        );

        seller = new User();
        seller.setId(1L);
        seller.setUsername("testuser");
        seller.setEmail("test@test.com");
        seller.setRole("user");

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole("admin");

        product = new Product("Gaming Laptop", "RTX 4060", new BigDecimal("75000.00"), 1L);
        product.setId(1L);
        product.setCreatedAt(LocalDateTime.now());

        productRequest = new ProductRequest();
        productRequest.setTitle("Gaming Laptop");
        productRequest.setDescription("RTX 4060");
        productRequest.setPrice(new BigDecimal("75000.00"));

        lenient().when(reviewRepository.countByProduct_Id(1L)).thenReturn(0L);
        lenient().when(reviewRepository.findAverageRatingByProductId(1L)).thenReturn(0.0);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── getAllProducts ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllProducts: should return all products")
    void getAllProducts_returnsAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));

        List<ProductResponse> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Gaming Laptop");
        assertThat(result.get(0).getSellerUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getAllProducts: should return empty list when no products")
    void getAllProducts_emptyRepository_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        List<ProductResponse> result = productService.getAllProducts();

        assertThat(result).isEmpty();
    }

    // ── getProductById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getProductById: should return product when found")
    void getProductById_found_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));

        ProductResponse result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Gaming Laptop");
        assertThat(result.getPrice()).isEqualByComparingTo("75000.00");
    }

    @Test
    @DisplayName("getProductById: should throw ResourceNotFoundException when not found")
    void getProductById_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ── createProduct ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createProduct: should create and return product with seller info")
    void createProduct_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(seller));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));

        ProductResponse result = productService.createProduct(productRequest);

        assertThat(result.getTitle()).isEqualTo("Gaming Laptop");
        assertThat(result.getSellerId()).isEqualTo(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // ── updateProduct ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProduct: owner can update their own product")
    void updateProduct_byOwner_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(seller));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));

        productRequest.setTitle("Updated Laptop");
        ProductResponse result = productService.updateProduct(1L, productRequest);

        assertThat(result).isNotNull();
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct: admin can update any product")
    void updateProduct_byAdmin_success() {
        // Admin is the current user
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", null, Collections.emptyList())
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product)); // owned by seller (id=1)
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser)); // admin id=2
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));

        ProductResponse result = productService.updateProduct(1L, productRequest);

        assertThat(result).isNotNull();
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct: non-owner non-admin throws AccessDeniedException")
    void updateProduct_byNonOwner_throwsAccessDenied() {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setUsername("testuser");
        otherUser.setRole("user");

        // product.sellerId = 1, but current user id = 99
        product.setSellerId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> productService.updateProduct(1L, productRequest))
            .isInstanceOf(AccessDeniedException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    // ── deleteProduct ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteProduct: owner can delete their own product")
    void deleteProduct_byOwner_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(seller));

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("deleteProduct: non-owner throws AccessDeniedException")
    void deleteProduct_byNonOwner_throwsAccessDenied() {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setUsername("testuser");
        otherUser.setRole("user");

        product.setSellerId(1L); // owned by someone else
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> productService.deleteProduct(1L))
            .isInstanceOf(AccessDeniedException.class);

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("deleteProduct: throws ResourceNotFoundException when product not found")
    void deleteProduct_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
