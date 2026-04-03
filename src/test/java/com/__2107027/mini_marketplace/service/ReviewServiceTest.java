package com.__2107027.mini_marketplace.service;

import com.__2107027.mini_marketplace.dto.ReviewRequest;
import com.__2107027.mini_marketplace.dto.ReviewResponse;
import com.__2107027.mini_marketplace.dto.ReviewSummaryResponse;
import com.__2107027.mini_marketplace.dto.ReviewUpdateRequest;
import com.__2107027.mini_marketplace.exception.ResourceNotFoundException;
import com.__2107027.mini_marketplace.model.Product;
import com.__2107027.mini_marketplace.model.Review;
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
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User currentUser;
    private User otherUser;
    private Product product;
    private Review review;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer", null, Collections.emptyList())
        );

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("buyer");
        currentUser.setRole("user");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("seller");
        otherUser.setRole("user");

        product = new Product("Gaming Laptop", "RTX 4060", new BigDecimal("75000.00"), 2L);
        product.setId(10L);
        product.setCreatedAt(LocalDateTime.now());

        review = new Review(currentUser, product, 5, "Excellent product");
        review.setId(100L);
        review.setCreatedAt(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("createReview: should create review successfully")
    void createReview_success() {
        ReviewRequest request = new ReviewRequest();
        request.setProductId(10L);
        request.setRating(5);
        request.setComment("Excellent product");

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(currentUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUser_IdAndProduct_Id(1L, 10L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.createReview(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getProductId()).isEqualTo(10L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRating()).isEqualTo(5);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("createReview: should reject duplicate review by same user for same product")
    void createReview_duplicate_throwsException() {
        ReviewRequest request = new ReviewRequest();
        request.setProductId(10L);
        request.setRating(4);
        request.setComment("Good");

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(currentUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUser_IdAndProduct_Id(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already reviewed");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("createReview: should reject review when user is the seller")
    void createReview_seller_throwsAccessDenied() {
        // Change current user to the seller
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("seller", null, Collections.emptyList())
        );

        ReviewRequest request = new ReviewRequest();
        request.setProductId(10L);
        request.setRating(5);
        request.setComment("My product");

        when(userRepository.findByUsername("seller")).thenReturn(Optional.of(otherUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product)); // product.sellerId = 2L = otherUser.id

        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("cannot review your own product");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("getReviewsForProduct: should return reviews for a product")
    void getReviewsForProduct_success() {
        review.setUser(currentUser);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProduct_IdOrderByCreatedAtDesc(10L)).thenReturn(List.of(review));

        List<ReviewResponse> responses = reviewService.getReviewsForProduct(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getProductTitle()).isEqualTo("Gaming Laptop");
    }

    @Test
    @DisplayName("getProductReviewSummary: should return count and average rating")
    void getProductReviewSummary_success() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.countByProduct_Id(10L)).thenReturn(2L);
        when(reviewRepository.findAverageRatingByProductId(10L)).thenReturn(4.5);

        ReviewSummaryResponse summary = reviewService.getProductReviewSummary(10L);

        assertThat(summary.getProductId()).isEqualTo(10L);
        assertThat(summary.getReviewCount()).isEqualTo(2L);
        assertThat(summary.getAverageRating()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("updateReview: owner can update their review")
    void updateReview_owner_success() {
        ReviewUpdateRequest request = new ReviewUpdateRequest();
        request.setRating(3);
        request.setComment("Updated comment");

        when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(currentUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.updateReview(100L, request);

        assertThat(response.getRating()).isEqualTo(3);
        assertThat(response.getComment()).isEqualTo("Updated comment");
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("updateReview: non-owner throws AccessDeniedException")
    void updateReview_nonOwner_throwsAccessDenied() {
        Review foreignReview = new Review(otherUser, product, 5, "Nice");
        foreignReview.setId(101L);

        when(reviewRepository.findById(101L)).thenReturn(Optional.of(foreignReview));
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(currentUser));

        ReviewUpdateRequest request = new ReviewUpdateRequest();
        request.setRating(2);
        request.setComment("Nope");

        assertThatThrownBy(() -> reviewService.updateReview(101L, request))
                .isInstanceOf(AccessDeniedException.class);

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("deleteReview: owner can delete review")
    void deleteReview_owner_success() {
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(review));
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(currentUser));

        reviewService.deleteReview(100L);

        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("getReviewsForProduct: throws ResourceNotFoundException when product missing")
    void getReviewsForProduct_productNotFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewsForProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}