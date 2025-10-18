package com.example.librarymanagement.service.impl;

import com.example.librarymanagement.dto.system.request.SystemConfigRequest;
import com.example.librarymanagement.dto.system.response.SystemConfigResponse;
import com.example.librarymanagement.entity.SystemConfig;
import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.exception.BadRequestException;
import com.example.librarymanagement.exception.ResourceNotFoundException;
import com.example.librarymanagement.repository.SystemConfigRepository;
import com.example.librarymanagement.repository.UserRepository;
import com.example.librarymanagement.service.inter.MaintenanceModeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceModeServiceImpl implements MaintenanceModeService {
    private final SystemConfigRepository systemConfigRepository;
    private final UserRepository userRepository;

    public static final String MAINTENANCE_MODE_KEY = "MAINTENANCE_MODE";
    public static final String MAINTENANCE_MODE_ON = "ON";
    public static final String MAINTENANCE_MODE_OFF = "OFF";

    @Cacheable(value = "maintenanceMode", key = "'status'")
    public boolean isMaintenanceModeEnabled() {
        SystemConfig config = systemConfigRepository.findByConfigKey(MAINTENANCE_MODE_KEY)
                .orElse(null);
        if (config == null) {
            return false; // Mặc định OFF nếu chưa có config
        }
        return MAINTENANCE_MODE_ON.equalsIgnoreCase(config.getConfigValue());
    }

    // Enable maintenance mode
    @Transactional
    @CacheEvict(value = "maintenanceMode", allEntries = true)
    public SystemConfigResponse enableMaintenanceMode(Authentication authentication) {
        log.warn("MAINTENANCE MODE: Enabling by user {}", authentication.getName());
        return updateMaintenanceMode(MAINTENANCE_MODE_ON, authentication);
    }

    // Disable maintenance mode
    @Transactional
    @CacheEvict(value = "maintenanceMode", allEntries = true)
    public SystemConfigResponse disableMaintenanceMode(Authentication authentication) {
        log.warn("MAINTENANCE MODE: Disabling by user {}", authentication.getName());
        return updateMaintenanceMode(MAINTENANCE_MODE_OFF, authentication);
    }

    // Toggle maintenance mode
    @Transactional
    @CacheEvict(value = "maintenanceMode", allEntries = true)
    public SystemConfigResponse toggleMaintenanceMode(Authentication authentication) {
        boolean currentStatus = isMaintenanceModeEnabled();
        String newStatus = currentStatus ? MAINTENANCE_MODE_OFF : MAINTENANCE_MODE_ON;
        log.info("MAINTENANCE MODE: Toggling from {} to {} by user {}",
                currentStatus ? "ON" : "OFF", newStatus, authentication.getName());
        return updateMaintenanceMode(newStatus, authentication);
    }

    // Get current maintenance mode status
    @Transactional(readOnly = true)
    public SystemConfigResponse getMaintenanceModeStatus() {
        SystemConfig config = systemConfigRepository.findByConfigKey(MAINTENANCE_MODE_KEY)
                .orElseGet(() -> {
                    SystemConfig defaultConfig = SystemConfig.builder()
                            .configKey(MAINTENANCE_MODE_KEY)
                            .configValue(MAINTENANCE_MODE_OFF)
                            .updatedAt(System.currentTimeMillis())
                            .build();
                    return systemConfigRepository.save(defaultConfig);
                });

        return mapToResponse(config);
    }

    // Get all system configs
    public List<SystemConfigResponse> getAllConfigs() {
        return systemConfigRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Update or create system config
    @Transactional
    @CacheEvict(value = "maintenanceMode", allEntries = true)
    public SystemConfigResponse updateConfig(SystemConfigRequest req, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String configKey = req.getConfigKey();

        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElse(SystemConfig.builder()
                        .configKey(configKey)
                        .build());

        String configValue = req.getConfigValue();
        config.setConfigValue(configValue);
        config.setUpdatedBy(user);
        config.setUpdatedAt(System.currentTimeMillis());

        config = systemConfigRepository.save(config);

        log.info("CONFIG UPDATED: {} = {} by {}",
                configKey, configValue, user.getEmail());

        return mapToResponse(config);
    }

    // Delete config
    @Transactional
    @CacheEvict(value = "maintenanceMode", allEntries = true)
    public void deleteConfig(String configKey) {
        if (MAINTENANCE_MODE_KEY.equals(configKey)) {
            throw new BadRequestException("Cannot delete MAINTENANCE_MODE config");
        }
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found: " + configKey));

        systemConfigRepository.delete(config);
        log.info("CONFIG DELETED: {}", configKey);
    }

    // ------------------------- helper -------------------------
    // Update maintenance mode
    public SystemConfigResponse updateMaintenanceMode(String status, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        SystemConfig config = systemConfigRepository.findByConfigKey(MAINTENANCE_MODE_KEY)
                .orElse(SystemConfig.builder()
                        .configKey(MAINTENANCE_MODE_KEY)
                        .build());

        config.setConfigValue(status);
        config.setUpdatedBy(user);
        config.setUpdatedAt(System.currentTimeMillis());

        config = systemConfigRepository.save(config);

        return mapToResponse(config);
    }

    // Map entity to response DTO
    public SystemConfigResponse mapToResponse(SystemConfig config) {
        return SystemConfigResponse.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .updatedAt(config.getUpdatedAt())
                .updatedByEmail(config.getUpdatedBy() != null ? config.getUpdatedBy().getEmail() : null)
                .build();
    }
}