package com.example.librarymanagement.service.inter;

import com.example.librarymanagement.dto.system.request.SystemConfigRequest;
import com.example.librarymanagement.dto.system.response.SystemConfigResponse;
import com.example.librarymanagement.entity.SystemConfig;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface MaintenanceModeService {
    boolean isMaintenanceModeEnabled();

    SystemConfigResponse enableMaintenanceMode(Authentication authentication);

    SystemConfigResponse disableMaintenanceMode(Authentication authentication);

    SystemConfigResponse toggleMaintenanceMode(Authentication authentication);

    SystemConfigResponse getMaintenanceModeStatus();

    List<SystemConfigResponse> getAllConfigs();

    SystemConfigResponse updateConfig(SystemConfigRequest req, Authentication authentication);

    void deleteConfig(String configKey);

    // -------------------------helper -------------------------
    SystemConfigResponse updateMaintenanceMode(String status, Authentication authentication);

    SystemConfigResponse mapToResponse(SystemConfig config);
}
