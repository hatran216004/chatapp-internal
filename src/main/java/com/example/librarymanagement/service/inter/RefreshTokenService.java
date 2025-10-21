package com.example.librarymanagement.service.inter;

import com.example.librarymanagement.entity.User;

public interface RefreshTokenService {
    void saveRefreshToken(User user, String refreshToken);
}
