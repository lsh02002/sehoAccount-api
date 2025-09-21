package com.sehoaccountapi.web.dto.categories;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String type;
    private String createdAt;
    private String updatedAt;
}
