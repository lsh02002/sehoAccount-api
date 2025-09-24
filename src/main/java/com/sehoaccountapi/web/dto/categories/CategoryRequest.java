package com.sehoaccountapi.web.dto.categories;

import com.sehoaccountapi.repository.category.CategoryType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryRequest {
    private String name;
    private Long parentId;
    private String type;
}
