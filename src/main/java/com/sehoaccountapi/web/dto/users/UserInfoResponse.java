package com.example.sehomallapi.web.dto.users;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponse {
    private Long userId;
    private String nickname;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private String gender;
    private String birthDate;
    private String userStatus;
    private String createAt;
    private String deleteAt;
}
