package com.sehoaccountapi.web.controller.users;

import com.sehoaccountapi.config.RestPage;
import com.sehoaccountapi.repository.user.userDetails.CustomUserDetails;
import com.sehoaccountapi.service.users.UserService;
import com.sehoaccountapi.web.dto.users.LoginRequest;
import com.sehoaccountapi.web.dto.users.SignupRequest;
import com.sehoaccountapi.web.dto.users.UserInfoResponse;
import com.sehoaccountapi.web.dto.users.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponse> signUp(@RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(userService.signUp(signupRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        List<Object> accessTokenAndRefreshTokenAndResponse = userService.login(loginRequest, httpServletRequest);
        httpServletResponse.addHeader("accessToken", accessTokenAndRefreshTokenAndResponse.get(0).toString());
        httpServletResponse.addHeader("refreshToken", accessTokenAndRefreshTokenAndResponse.get(1).toString());

        return ResponseEntity.ok((UserResponse) accessTokenAndRefreshTokenAndResponse.get(2));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<UserResponse> logout(@AuthenticationPrincipal CustomUserDetails customUserDetails, HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(userService.logout(customUserDetails.getEmail(), request, response));
    }

    @DeleteMapping("/withdrawal")
    public ResponseEntity<UserResponse> withdrawal(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(userService.withdrawal(customUserDetails.getEmail()));
    }

    @GetMapping("/info")
    public ResponseEntity<UserInfoResponse> getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(userService.getUserInfo(customUserDetails));
    }

    @GetMapping("/is-nickname-existed/{nickname}")
    public ResponseEntity<Boolean> isNicknameExisted(@PathVariable(name = "nickname") String nickname) {
        return ResponseEntity.ok(userService.isNicknameExisted(nickname));
    }

    @GetMapping("/is-email-existed/{email}")
    public ResponseEntity<Boolean> isEmailExisted(@PathVariable(name = "email") String email) {
        return ResponseEntity.ok(userService.isEmailExisted(email));
    }

    @GetMapping("/entrypoint")
    public ResponseEntity<Map<String, String>> entrypoint(
            @RequestParam(name = "accessToken", required = false) String token) {

        String message = (token == null)
                ? "로그인이 필요합니다."
                : "로그인(JWT 토큰)이 만료되었습니다. 다시 로그인하세요.";

        // 401 Unauthorized 로 통일 (권한 부족은 403)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", message, "path", "/user/entrypoint"));
    }

    @GetMapping(value = "/access-denied", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Map<String, Object>> accessDeniedException(
            @RequestParam(name = "roles", required = false) String roles,
            HttpServletRequest request) {

        String message = (roles == null)
                ? "권한이 설정되지 않았습니다."
                : "권한이 없습니다.";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("path", request.getRequestURI());
        if (roles != null) body.put("roles", roles);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body); // 403
    }

    @GetMapping("/test1")
    public ResponseEntity<Object> test1(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(customUserDetails.toString());
    }

    @GetMapping("/test2")
    public ResponseEntity<String> test2() {
        return ResponseEntity.ok("Jwt 토큰이 상관없는 EntryPoint 테스트입니다.");
    }

    //관리자 모듈

    @PostMapping("/admin-login")
    public ResponseEntity<UserResponse> adminLogin(@RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        List<Object> accessTokenAndRefreshTokenAndResponse = userService.adminLogin(loginRequest, httpServletRequest);
        httpServletResponse.addHeader("accessToken", accessTokenAndRefreshTokenAndResponse.get(0).toString());
        httpServletResponse.addHeader("refreshToken", accessTokenAndRefreshTokenAndResponse.get(1).toString());

        return ResponseEntity.ok((UserResponse) accessTokenAndRefreshTokenAndResponse.get(2));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping("/all-users-info")
    public ResponseEntity<RestPage<UserInfoResponse>> getAllUsersInfo(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsersInfo(pageable));
    }
}
