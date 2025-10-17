package com.sehoaccountapi.config.security;

import com.sehoaccountapi.config.filters.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers((headers) ->
                        headers.xssProtection(Customizer.withDefaults())
                                .contentSecurityPolicy(Customizer.withDefaults()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> {
                    e.authenticationEntryPoint(authenticationEntryPoint);
                    e.accessDeniedHandler(accessDeniedHandler);
                })
                .authorizeHttpRequests(a ->
                        a
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/books/**").hasRole("USER")
                                .requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("USER")
                                .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("USER")
                                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("USER")

                                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("USER")
                                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("USER")

                                .requestMatchers(HttpMethod.GET, "/api/transactions/**").hasRole("USER")
                                .requestMatchers(HttpMethod.POST, "/api/transactions/**").hasRole("USER")
                                .requestMatchers(HttpMethod.PUT, "/api/transactions/**").hasRole("USER")

                                .requestMatchers(HttpMethod.GET, "/user/info/**", "/user/test1/**", "/user/all-users-info/**").hasRole("USER")
                                .requestMatchers(HttpMethod.DELETE, "/user/logout/**", "/user/withdrawal/**").hasRole("USER")
                                .requestMatchers(HttpMethod.GET, "/user/entrypoint/**", "/user/access-denied/**").permitAll()
                                // 지정하지 않은 나머지는 Jwt 토큰이 상관없는 엔트리포인트입니다.
                                .requestMatchers("/**").permitAll())
                .addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("*"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setExposedHeaders(List.of("accesstoken", "refreshtoken"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS"));
        corsConfiguration.setMaxAge(1000L * 60 * 60);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
