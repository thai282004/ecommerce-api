package com.thai2004z.ecommerce_api.service;

import com.thai2004z.ecommerce_api.dto.request.CategoryRequest;
import com.thai2004z.ecommerce_api.dto.response.CategoryResponse;
import com.thai2004z.ecommerce_api.entity.Category;
import com.thai2004z.ecommerce_api.exception.ResourceNotFoundException;
import com.thai2004z.ecommerce_api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    public CategoryResponse create(CategoryRequest req) {
        Category category = Category.builder()
                .name(req.getName())
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        category.setName(req.getName());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
