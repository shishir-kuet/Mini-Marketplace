package com.__2107027.mini_marketplace.service;

import com.__2107027.mini_marketplace.dto.CategoryRequest;
import com.__2107027.mini_marketplace.dto.CategoryResponse;
import com.__2107027.mini_marketplace.exception.ResourceNotFoundException;
import com.__2107027.mini_marketplace.model.Category;
import com.__2107027.mini_marketplace.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category();
        sampleCategory.setId(1L);
        sampleCategory.setName("Electronics");
        sampleCategory.setDescription("Electronic devices and accessories");
        sampleCategory.setCreatedAt(LocalDateTime.now());
    }

    // ── getAllCategories ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllCategories: should return all categories")
    void getAllCategories_returnsAll() {
        when(categoryRepository.findAll()).thenReturn(List.of(sampleCategory));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("getAllCategories: should return empty list when no categories exist")
    void getAllCategories_empty_returnsEmptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).isEmpty();
    }

    // ── getCategoryById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getCategoryById: should return category when found")
    void getCategoryById_found_returnsResponse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("getCategoryById: should throw ResourceNotFoundException when not found")
    void getCategoryById_notFound_throwsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ── createCategory ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createCategory: should create category successfully")
    void createCategory_success_returnsResponse() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");
        request.setDescription("Fiction and non-fiction books");

        Category savedCategory = new Category();
        savedCategory.setId(2L);
        savedCategory.setName("Books");
        savedCategory.setDescription("Fiction and non-fiction books");
        savedCategory.setCreatedAt(LocalDateTime.now());

        when(categoryRepository.existsByName("Books")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Books");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("createCategory: should throw IllegalArgumentException when name already exists")
    void createCategory_duplicateName_throwsException() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");
        request.setDescription("Duplicate category");

        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Electronics");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ── updateCategory ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateCategory: should update name and description successfully")
    void updateCategory_success_returnsUpdatedResponse() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Consumer Electronics");
        request.setDescription("Updated description");

        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Consumer Electronics");
        updatedCategory.setDescription("Updated description");
        updatedCategory.setCreatedAt(sampleCategory.getCreatedAt());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.existsByName("Consumer Electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        CategoryResponse result = categoryService.updateCategory(1L, request);

        assertThat(result.getName()).isEqualTo("Consumer Electronics");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("updateCategory: same name update should not check for duplicates")
    void updateCategory_sameName_noDuplicateCheck() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics"); // same name, no duplicate check needed
        request.setDescription("New description");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);

        categoryService.updateCategory(1L, request);

        verify(categoryRepository, never()).existsByName(any());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("updateCategory: should throw ResourceNotFoundException when category not found")
    void updateCategory_notFound_throwsException() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, request))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ── deleteCategory ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteCategory: should delete category when found")
    void deleteCategory_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).delete(sampleCategory);
    }

    @Test
    @DisplayName("deleteCategory: should throw ResourceNotFoundException when not found")
    void deleteCategory_notFound_throwsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
