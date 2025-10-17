package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_config")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "cfg_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "cfg_value", nullable = false)
    private String configValue;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @ManyToOne // Một user có thể chỉnh sửa nhiều cấu hình hệ thống khác nhau
    /* Cho biết cột foreign key trong bảng system_config tên là updated_by.
       Cột này sẽ liên kết (JOIN) đến cột id của bảng users. */
    @JoinColumn(name = "updated_by") //
    private User updatedBy;


    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
}
