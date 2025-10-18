package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER) // fetch = FetchType.EAGER: khi load User, JPA tự động load luôn Role.
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "deleted_at")
    private Long deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    /* Trong UserProfile, cột user_id là foreign key trỏ về bảng users.id.
    Cột user_id nằm ở bảng user_profile (nên UserProfile là bên sở hữu mối quan hệ, gọi là owning side).
    mappedBy = "user": Cột user bên trong class UserProfile mới là bên điều khiển mối quan hệ.*/
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile userProfile;

    // Chạy trước khi entity được lưu mới (insert)
    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }

    // Chạy trước khi entity được cập nhật (update)
    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }

    /*
    * | Thành phần                               | Giải thích                                        |
      | ---------------------------------------- | ------------------------------------------------- |
      | `GrantedAuthority`                       | Interface mô tả 1 quyền                           |
      | `SimpleGrantedAuthority`                 | Implement mặc định của quyền                      |
      | `List.of(...)`                           | Tạo danh sách quyền                               |
      | `role.getName()`                         | Lấy tên quyền (VD: `"ROLE_USER"`)                 |
      | Trả về list này trong `getAuthorities()` | Spring Security dùng để xác định user có quyền gì |
    * */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getName()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    public enum UserStatus {
        ACTIVE, LOCKED, PENDING
    }
}
























































