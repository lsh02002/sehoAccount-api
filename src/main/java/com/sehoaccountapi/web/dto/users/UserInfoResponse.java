package com.sehoaccountapi.web.dto.users;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponse {
    private Long userId;
    private String nickname;
    private String email;
    private String userStatus;
    private String createdAt;
    private String deletedAt;
}
