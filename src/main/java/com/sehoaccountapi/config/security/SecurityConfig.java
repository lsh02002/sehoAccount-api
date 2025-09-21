package com.example.sehomallapi.config.security;

import com.example.sehomallapi.config.filters.JwtFilter;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers((headers) ->
                headers.xssProtection(Customizer.withDefaults())
                        .contentSecurityPolicy(Customizer.withDefaults()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(c-> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e->{
                    e.authenticationEntryPoint(new AuthenticationEntryPointImpl());
                    e.accessDeniedHandler(new AccessDeniedHandlerImpl());
                })
                .authorizeHttpRequests(a->
                        a
                                // cart controller
                                .requestMatchers(HttpMethod.POST, "/cart/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.GET, "/cart/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.PATCH, "/cart/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.DELETE, "/cart/**").hasAnyRole("USER")
                                // heart controller
                                .requestMatchers(HttpMethod.POST, "/heart/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.GET, "/heart/is-hearted/**", "/heart/user/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.DELETE, "/heart/**").hasAnyRole("USER")
                                // item controller
                                .requestMatchers(HttpMethod.POST, "/api/items/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.GET, "/api/items/user/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasAnyRole("USER")
                                // pay controller
                                .requestMatchers(HttpMethod.POST, "/api/payments/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.GET, "/api/payments/**", "/api/payments/user/**", "/api/payments/status/**").hasAnyRole("USER")
                                // review controller
                                .requestMatchers(HttpMethod.POST, "/review/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.GET, "/review/user/**","/review/unreviewed-items/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.PUT, "/review/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.DELETE, "/review/**").hasAnyRole("USER")
                                // notice controller
                                .requestMatchers(HttpMethod.POST, "/api/notices/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.GET, "/api/notices/user/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.PUT, "/api/notices/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.DELETE, "/api/notices/**").hasAnyRole("USER")
                                // user controller
                                .requestMatchers(HttpMethod.GET, "/user/info/**", "/user/hist/**", "/user/test1/**","/user/all-users-info/**").hasAnyRole("USER")
                                .requestMatchers(HttpMethod.DELETE, "/user/logout/**", "/user/withdrawal").hasAnyRole("USER")
                                // 지정하지 않은 나머지는 Jwt 토큰이 상관없는 엔트리포인트입니다.
                                .requestMatchers( "/**").permitAll())
                .addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("*"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setExposedHeaders(List.of("accessToken", "refreshToken"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "PUT", "POST","PATCH","DELETE","OPTIONS"));
        corsConfiguration.setMaxAge(1000L*60*60);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
