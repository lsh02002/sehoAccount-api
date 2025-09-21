package com.example.sehomallapi.web.dto.users;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private int code;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;
}
