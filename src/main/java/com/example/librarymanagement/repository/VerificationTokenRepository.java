package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(Integer userId,
                                                                                            VerificationToken.TokenPurpose purose);

    /*Custom query trong repository dùng để xoá tất cả các token đã hết hạn trong bảng verification_token
    * Vì đây là câu lệnh thay đổi dữ liệu (DELETE),
     nên phải có @Modifying để báo cho Spring Data JPA biết:
    “Đây không phải là câu query SELECT,
     mà là câu query thực hiện thao tác thay đổi dữ liệu (update / delete / insert).”*/
    @Modifying
    @Query("DELETE FROM VerificationToken v WHERE v.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
