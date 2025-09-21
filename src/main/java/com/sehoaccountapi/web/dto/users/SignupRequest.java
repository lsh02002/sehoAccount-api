package com.example.sehomallapi.web.dto.users;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    private String email;
    private String name;
    private String password;
    private String passwordConfirm;
    private String nickname;
    private String phoneNumber;
    private String address;
    private String gender;
    private String birthDate;
}
