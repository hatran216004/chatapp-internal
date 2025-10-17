package com.example.librarymanagement.dto.system.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemConfigResponse {
    private Integer id;
    private String configKey;
    private String configValue;
    private Long updatedAt;
    private String updatedByEmail;
}
