package com.example.sehomallapi.web.dto.users;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponse {
    private Long userId;
    private String nickname;
}
