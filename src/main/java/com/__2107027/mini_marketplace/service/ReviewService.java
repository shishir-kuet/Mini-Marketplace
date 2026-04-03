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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private ReviewResponse toResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUserId(review.getUser().getId());
        response.setUsername(review.getUser().getUsername());
        response.setProductId(review.getProduct().getId());
        response.setProductTitle(review.getProduct().getTitle());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }

    public List<ReviewResponse> getReviewsForProduct(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        return reviewRepository.findByProduct_IdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReviewSummaryResponse getProductReviewSummary(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ReviewSummaryResponse summary = new ReviewSummaryResponse();
        summary.setProductId(productId);
        summary.setReviewCount(reviewRepository.countByProduct_Id(productId));
        summary.setAverageRating(reviewRepository.findAverageRatingByProductId(productId));
        return summary;
    }

    public List<ReviewResponse> getMyReviews() {
        User currentUser = getCurrentUser();
        return reviewRepository.findByUser_IdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReviewResponse createReview(ReviewRequest request) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (product.getSellerId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot review your own product");
        }

        if (reviewRepository.existsByUser_IdAndProduct_Id(currentUser.getId(), product.getId())) {
            throw new IllegalArgumentException("You have already reviewed this product");
        }

        Review review = new Review(currentUser, product, request.getRating(), request.getComment());
        return toResponse(reviewRepository.save(review));
    }

    public ReviewResponse updateReview(Long id, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        User currentUser = getCurrentUser();
        boolean isOwner = review.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only update your own review");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return toResponse(reviewRepository.save(review));
    }

    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        User currentUser = getCurrentUser();
        boolean isOwner = review.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only delete your own review");
        }

        reviewRepository.delete(review);
    }
}