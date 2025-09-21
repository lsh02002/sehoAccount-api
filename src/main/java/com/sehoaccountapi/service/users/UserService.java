package com.sehoaccountapi.service.users;

import com.sehoaccountapi.config.RestPage;
import com.sehoaccountapi.config.redis.RedisUtil;
import com.sehoaccountapi.config.security.JwtTokenProvider;
import com.sehoaccountapi.repository.user.User;
import com.sehoaccountapi.repository.user.UserRepository;
import com.sehoaccountapi.repository.user.refreshToken.RefreshToken;
import com.sehoaccountapi.repository.user.refreshToken.RefreshTokenRepository;
import com.sehoaccountapi.repository.user.userDetails.CustomUserDetails;
import com.sehoaccountapi.repository.user.userRoles.Roles;
import com.sehoaccountapi.repository.user.userRoles.RolesRepository;
import com.sehoaccountapi.repository.user.userRoles.UserRoles;
import com.sehoaccountapi.repository.user.userRoles.UserRolesRepository;
import com.sehoaccountapi.service.exceptions.AccessDeniedException;
import com.sehoaccountapi.service.exceptions.BadRequestException;
import com.sehoaccountapi.service.exceptions.ConflictException;
import com.sehoaccountapi.service.exceptions.NotFoundException;
import com.sehoaccountapi.web.dto.users.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final UserRolesRepository userRolesRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    private final PasswordEncoder passwordEncoder;

    private static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    private static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    @PostConstruct
    private void insertRoleUserAndRoleAdminToNewDb() {
        //db를 새로 생성할 때 roles(ROLE_USER)초기값 생성
        Roles roleUser = rolesRepository.findByName("ROLE_USER");

        if (roleUser == null) {
            rolesRepository.save(Roles.builder()
                    .name("ROLE_USER")
                    .build());
        }

        //db를 새로 생성할 때 roles(ROLE_ADMIN)초기값 생성
        Roles roleAdmin = rolesRepository.findByName("ROLE_ADMIN");

        if (roleAdmin == null) {
            rolesRepository.save(Roles.builder()
                    .name("ROLE_ADMIN")
                    .build());
        }
    }

    @Transactional
    public UserResponse signUp(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        String password = signupRequest.getPassword();

        if (!email.matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$")) {
            throw new BadRequestException("이메일을 정확히 입력해주세요.", email);
        } else if (signupRequest.getNickname().matches("01\\d{9}")) {
            throw new BadRequestException("전화번호를 이름으로 사용할수 없습니다.", signupRequest.getNickname());
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("이미 입력하신 " + email + " 이메일로 가입된 계정이 있습니다.", email);
        } else if (signupRequest.getNickname().trim().isEmpty() || signupRequest.getNickname().length() > 30) {
            throw new BadRequestException("닉네임은 비어있지 않고 30자리 이하여야 합니다.", signupRequest.getNickname());
        } else if (!signupRequest.getNickname().matches("^[A-Za-z][A-Za-z0-9]*$")) {
            throw new BadRequestException("닉네임은 영문으로 시작하고 영어 숫자 조합이어야 합니다.", signupRequest.getNickname());
        } else if (userRepository.existsByNickname(signupRequest.getNickname())) {
            throw new BadRequestException("이미 입력하신 " + signupRequest.getNickname() + "닉네임으로 가입된 계정이 있습니다.", signupRequest.getNickname());
        } else if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]+$")
                || !(password.length() >= 8 && password.length() <= 20)) {
            throw new BadRequestException("비밀번호는 8자 이상 20자 이하 숫자와 영문소문자 조합 이어야 합니다.", password);
        } else if (!signupRequest.getPasswordConfirm().equals(password)) {
            throw new BadRequestException("비밀번호와 비밀번호 확인이 같지 않습니다.", "password : " + password + ", password_confirm : " + signupRequest.getPasswordConfirm());
        }

        signupRequest.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        Roles roles = rolesRepository.findByName("ROLE_USER");

        User user = User.builder()
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword())
                .nickname(signupRequest.getNickname())
                .userStatus("정상")
                .build();

        userRepository.save(user);

        userRolesRepository.save(UserRoles.builder()
                .user(user)
                .roles(roles)
                .build());

        SignupResponse signupResponse = SignupResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();

        return new UserResponse(HttpStatus.OK.value(), user.getNickname() + "님 회원 가입 완료 되었습니다.", signupResponse);
    }

    @Transactional
    public List<Object> login(LoginRequest request, HttpServletRequest httpServletRequest) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new BadRequestException("이메일이나 비밀번호 값이 비어있습니다.", "email : " + request.getEmail() + ", password : " + request.getPassword());
        }
        User user;

        if (request.getEmail().matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$")) {
            user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new NotFoundException("입력하신 이메일의 계정을 찾을 수 없습니다.", request.getEmail()));
        } else {
            throw new BadRequestException("이메일이나 비밀번호가 잘못 입력되었습니다.", null);
        }
        String p1 = user.getPassword();

        if (!passwordEncoder.matches(request.getPassword(), p1)) {
            throw new BadRequestException("이메일이나 비밀번호가 잘못 입력되었습니다.", null);
        }

        if (user.getUserStatus().equals("탈퇴")) {
            throw new AccessDeniedException("탈퇴한 계정입니다.", request.getEmail());
        }

        List<String> roles = user.getUserRoles().stream()
                .map(UserRoles::getRoles).map(Roles::getName).toList();

        SignupResponse signupResponse = SignupResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();

        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        RefreshToken newToken = RefreshToken.builder()
                .authId(user.getId().toString())
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .build();

        refreshTokenRepository.save(newToken);

        UserResponse authResponse = new UserResponse(HttpStatus.OK.value(), "로그인에 성공 하였습니다.", signupResponse);

        return Arrays.asList(jwtTokenProvider.createAccessToken(user.getEmail()), newRefreshToken, authResponse);
    }

    public UserInfoResponse getUserInfo(CustomUserDetails customUserDetails) {
        String createdAt = customUserDetails.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
        String deletedAt = customUserDetails.getDeletedAt() != null ? customUserDetails.getDeletedAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) : null;

        return UserInfoResponse.builder()
                .userId(customUserDetails.getId())
                .nickname(customUserDetails.getNickname())
                .email(customUserDetails.getEmail())
                .userStatus(customUserDetails.getUserStatus())
                .createdAt(createdAt)
                .deletedAt(deletedAt)
                .build();
    }

    @Transactional
    public UserResponse logout(String email, HttpServletRequest request, HttpServletResponse response) {
        String accessToken = request.getHeader("accessToken");

        if (email == null) {
            throw new BadRequestException("유저 정보가 비어있습니다.", null);
        }

        RefreshToken deletedToken = refreshTokenRepository.findByEmail(email);
        if (deletedToken != null) {
            refreshTokenRepository.delete(deletedToken);
        }

        if (jwtTokenProvider.validateToken(accessToken)) {
            redisUtil.setBlackList(accessToken, "accessToken", 30);
        }

        return new UserResponse(HttpStatus.OK.value(), "로그아웃에 성공 하였습니다.", null);
    }

    @Transactional
    public UserResponse withdrawal(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("계정을 찾을 수 없습니다. 다시 로그인 해주세요.", email));

        if (user.getUserStatus().equals("탈퇴")) {
            throw new BadRequestException("이미 탈퇴처리된 회원 입니다.", email);
        }
        user.setUserStatus("탈퇴");
        user.setDeletedAt(LocalDateTime.now());

        return new UserResponse(200, "회원탈퇴 완료 되었습니다.", user.getNickname());
    }

    public boolean isNicknameExisted(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean isEmailExisted(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public List<Object> adminLogin(LoginRequest request, HttpServletRequest httpServletRequest) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new BadRequestException("이메일이나 비밀번호 값이 비어있습니다.", "email : " + request.getEmail() + ", password : " + request.getPassword());
        }
        User user;

        if (request.getEmail().matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$")) {
            user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new NotFoundException("입력하신 이메일의 계정을 찾을 수 없습니다.", request.getEmail()));
        } else {
            throw new BadRequestException("이메일이나 비밀번호가 잘못 입력되었습니다.", null);
        }
        String p1 = user.getPassword();

        if (!passwordEncoder.matches(request.getPassword(), p1)) {
            throw new BadRequestException("이메일이나 비밀번호가 잘못 입력되었습니다.", null);
        }

        if (user.getUserStatus().equals("탈퇴")) {
            throw new AccessDeniedException("탈퇴한 계정입니다.", request.getEmail());
        }

        List<String> roles = user.getUserRoles().stream()
                .map(UserRoles::getRoles).map(Roles::getName).toList();

        if (!roles.contains("ROLE_ADMIN")) {
            throw new BadRequestException("관리자 권한이 없습니다.", request.getEmail());
        }

        SignupResponse signupResponse = SignupResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();

        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        RefreshToken newToken = RefreshToken.builder()
                .authId(user.getId().toString())
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .build();

        refreshTokenRepository.save(newToken);

        UserResponse authResponse = new UserResponse(HttpStatus.OK.value(), "로그인에 성공 하였습니다.", signupResponse);

        return Arrays.asList(jwtTokenProvider.createAccessToken(user.getEmail()), newRefreshToken, authResponse);
    }

    public RestPage<UserInfoResponse> getAllUsersInfo(Pageable pageable) {
        return new RestPage<>(userRepository.findAll(pageable)
                .map(user -> UserInfoResponse.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .userStatus(user.getUserStatus())
                        .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                        .deletedAt(user.getDeletedAt() != null ? user.getDeletedAt().toString() : null)
                        .build()));
    }
}
