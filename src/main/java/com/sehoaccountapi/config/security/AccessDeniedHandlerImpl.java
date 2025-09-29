package com.sehoaccountapi.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException accessDeniedException
    ) throws IOException {

        boolean isApi = isApiRequest(request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities =
                (authentication == null || authentication.getAuthorities() == null)
                        ? Collections.emptyList()
                        : authentication.getAuthorities();

        if (isApi) {
            // ✅ API 응답: 403 JSON
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.setContentType("application/json;charset=UTF-8");

            String rolesJson = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(r -> "\"" + escapeJson(r) + "\"")
                    .collect(Collectors.joining(","));

            String body = "{"
                    + "\"error\":\"forbidden\","
                    + "\"message\":\"Access denied\","
                    + "\"roles\":[" + rolesJson + "],"
                    + "\"path\":\"" + escapeJson(request.getRequestURI()) + "\""
                    + "}";
            response.getWriter().write(body);
            response.getWriter().flush();
            return;
        }

        // 🌐 페이지 요청만 리다이렉트 (303: 이후 메서드를 GET으로 바꿈)
        String rolesParam = authorities.isEmpty()
                ? ""
                : "?roles=" + URLEncoder.encode(
                authorities.stream().map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")),
                StandardCharsets.UTF_8
        );

        response.setStatus(HttpServletResponse.SC_SEE_OTHER); // 303 See Other
        response.setHeader("Location", "/user/access-denied" + rolesParam);
    }

    private boolean isApiRequest(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String xrw = req.getHeader("X-Requested-With");
        String uri = req.getRequestURI();
        String method = req.getMethod();

        // API로 간주할 힌트들: JSON 기대, AJAX, /api prefix, 비-GET 메서드(DELETE/PUT/PATCH)
        return (accept != null && accept.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(xrw)
                || (uri != null && (uri.startsWith("/api/") || uri.startsWith("/user/"))) // 필요시 경로 조정
                || "DELETE".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

