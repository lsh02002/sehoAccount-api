package com.sehoaccountapi.web.dto.books;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BookRequest {
    private String name;
    private String description;
}
