package com.sehoaccountapi.service.security;

import com.sehoaccountapi.repository.user.User;
import com.sehoaccountapi.repository.user.UserRepository;
import com.sehoaccountapi.repository.user.userDetails.CustomUserDetails;
import com.sehoaccountapi.repository.user.userRoles.Roles;
import com.sehoaccountapi.repository.user.userRoles.UserRoles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Primary
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
//    @Cacheable(key = "#email", value = "auth")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("(토큰에러) 해당 이메일을 찾을 수 없습니다."));

        return CustomUserDetails.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userStatus(user.getUserStatus())
                .createdAt(user.getCreatedAt())
                .deletedAt(user.getDeletedAt())
                .authorities(user.getUserRoles()
                        .stream().map(UserRoles::getRoles)
                        .map(Roles::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}
