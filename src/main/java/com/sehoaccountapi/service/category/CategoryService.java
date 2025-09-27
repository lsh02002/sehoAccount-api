package com.sehoaccountapi.service.category;

import com.sehoaccountapi.repository.category.Category;
import com.sehoaccountapi.repository.category.CategoryRepository;
import com.sehoaccountapi.repository.transaction.TransactionType;
import com.sehoaccountapi.service.exceptions.BadRequestException;
import com.sehoaccountapi.service.exceptions.NotFoundException;
import com.sehoaccountapi.web.dto.categories.CategoryRequest;
import com.sehoaccountapi.web.dto.categories.CategoryResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void init(){
        Optional<Category> category = categoryRepository.findCategoryByName("ALL");

        if(category.isEmpty()){
            categoryRepository.save(Category.builder()
                    .parent(null)
                    .type(TransactionType.ALL)
                    .name("ALL")
                    .build());
        }
    }

    @Transactional
    public List<CategoryResponse> getCategories(){
        return categoryRepository.findAll().stream().map(this::converToCategoryResponse).toList();
    }

    @Transactional
    public CategoryResponse getCategoryById(Long id){
        return categoryRepository.findById(id)
                .map(this::converToCategoryResponse)
                .orElseThrow(()->new NotFoundException("해당 카테고리를 찾을 수 없습니다.", id));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest){
        Category parent = categoryRepository.findById(categoryRequest.getParentId())
                .orElseThrow(()->new NotFoundException("부모 카테고리를 찾을 수 없습니다.", categoryRequest.getParentId()));

        if (!(categoryRequest.getType().equals("INCOME")
                || categoryRequest.getType().equals("EXPENSE"))) {
            throw new BadRequestException("분류 타입은 'INCOME' 과 'EXPENSE' 중 하나입니다.", null);
        }

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .type(TransactionType.valueOf(categoryRequest.getType()))
                .parent(parent)
                .build();

        Category savedCategory = categoryRepository.save(category);

        return converToCategoryResponse(savedCategory);
    }

    @Transactional
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }

    private CategoryResponse converToCategoryResponse(Category category){
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType().toString())
                .createdAt(category.getCreatedAt().toString())
                .updatedAt(category.getUpdatedAt().toString())
                .build();
    }
}
