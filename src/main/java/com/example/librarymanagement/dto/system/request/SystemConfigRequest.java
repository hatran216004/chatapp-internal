package com.example.librarymanagement.dto.system.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SystemConfigRequest {
    @NotBlank(message = "Config key is required")
    private String configKey;

    @NotBlank(message = "Config value is required")
    @Pattern(regexp = "^(ON|OFF)$", message = "Value must be ON or OFF")
    private String configValue;
}
