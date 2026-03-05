package com.__2107027.mini_marketplace.service;

import com.__2107027.mini_marketplace.dto.ProductRequest;
import com.__2107027.mini_marketplace.dto.ProductResponse;
import com.__2107027.mini_marketplace.exception.ResourceNotFoundException;
import com.__2107027.mini_marketplace.model.Product;
import com.__2107027.mini_marketplace.model.User;
import com.__2107027.mini_marketplace.repository.ProductRepository;
import com.__2107027.mini_marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

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

    private ProductResponse toResponse(Product product) {
        String sellerUsername = userRepository.findById(product.getSellerId())
                .map(User::getUsername)
                .orElse("unknown");
        return new ProductResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getSellerId(),
                sellerUsername,
                product.getCreatedAt()
        );
    }

    // ── Public reads ──────────────────────────────────────────────────────────

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toResponse(product);
    }

    public List<ProductResponse> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchProducts(String title) {
        return productRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getMyProducts() {
        User currentUser = getCurrentUser();
        return getProductsBySeller(currentUser.getId());
    }

    // ── Authenticated writes ───────────────────────────────────────────────────

    public ProductResponse createProduct(ProductRequest request) {
        User currentUser = getCurrentUser();
        Product product = new Product(
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                currentUser.getId()
        );
        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        User currentUser = getCurrentUser();
        boolean isOwner = product.getSellerId().equals(currentUser.getId());
        boolean isAdmin = currentUser.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only update your own products");
        }

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        return toResponse(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        User currentUser = getCurrentUser();
        boolean isOwner = product.getSellerId().equals(currentUser.getId());
        boolean isAdmin = currentUser.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only delete your own products");
        }

        productRepository.delete(product);
    }
}
