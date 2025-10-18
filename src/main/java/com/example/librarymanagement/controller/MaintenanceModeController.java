package com.example.librarymanagement.controller;

import com.example.librarymanagement.dto.ApiResponse;
import com.example.librarymanagement.dto.system.request.SystemConfigRequest;
import com.example.librarymanagement.dto.system.response.SystemConfigResponse;
import com.example.librarymanagement.service.inter.MaintenanceModeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/system")
@RequiredArgsConstructor
// POST dùng để kích hoạt hành động (action), không nhất thiết tạo resource mới.
public class MaintenanceModeController {
    private final MaintenanceModeService maintenanceModeService;

    @GetMapping("/maintenance-mode")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> getMaintenanceModeStatus() {
        SystemConfigResponse res = maintenanceModeService.getMaintenanceModeStatus();
        return ResponseEntity.ok(ApiResponse.success("Maintenance mode status retrieved", res));
    }

    @PostMapping("/maintenance-mode/enable")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> enableMaintenanceMode(Authentication authentication) {
        SystemConfigResponse res = maintenanceModeService.enableMaintenanceMode(authentication);
        return ResponseEntity.ok(ApiResponse.success("Maintenance mode enabled", res));
    }

    @PostMapping("/maintenance-mode/disable")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> disableMaintenanceMode(Authentication authentication) {
        SystemConfigResponse res = maintenanceModeService.disableMaintenanceMode(authentication);
        return ResponseEntity.ok(ApiResponse.success("Maintenance mode disabled", res));
    }

    @PostMapping("/maintenance-mode/toggle")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> toggleMaintenanceMode(Authentication authentication) {
        SystemConfigResponse res = maintenanceModeService.toggleMaintenanceMode(authentication);
        return ResponseEntity.ok(ApiResponse.success("Maintenance mode toggled", res));
    }

    @GetMapping("/configs")
    public ResponseEntity<ApiResponse<List<SystemConfigResponse>>> getAllConfigs() {
        List<SystemConfigResponse> list = maintenanceModeService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success("System configs retrieved", list));
    }

    @PutMapping("/configs")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> updateConfig(
            @Valid @RequestBody SystemConfigRequest req,
            Authentication authentication) {
        SystemConfigResponse res = maintenanceModeService.updateConfig(req, authentication);
        return ResponseEntity.ok(ApiResponse.success("Config updated successfully", res));
    }

    @DeleteMapping("/configs/{configKey}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable String configKey) {
        maintenanceModeService.deleteConfig(configKey);
        return ResponseEntity.ok(ApiResponse.success("Config deleted successfully"));
    }
}
