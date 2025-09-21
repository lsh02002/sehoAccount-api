package com.example.sehomallapi.repository.users.refreshToken;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    RefreshToken findByEmail(String email);
}
