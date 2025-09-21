package com.sehoaccountapi.web.dto.books;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String name;
    private String description;
    private String nickname;
    private String createdAt;
    private String updatedAt;
}
