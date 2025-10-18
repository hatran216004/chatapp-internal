package com.example.librarymanagement.service.inter;

import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.VerificationToken;

public interface EmailTokenService {
    public String createVerificationToken(User user, VerificationToken.TokenPurpose purpose);

    public String createVerificationToken(User user,
                                          VerificationToken.TokenPurpose purpose,
                                          String newEmail);

    public VerificationToken validateToken(String token, VerificationToken.TokenPurpose purpose);

    public void cleanupExpiredTokens();
}
