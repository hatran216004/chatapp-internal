package com.example.librarymanagement.filter;

import com.example.librarymanagement.dto.ApiResponse;
import com.example.librarymanagement.service.MaintenanceModeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceModeFilter extends OncePerRequestFilter {
    private final MaintenanceModeService maintenanceModeService;
    private final ObjectMapper objectMapper;

    private static final List<String> ALLOWED_ENDPOINTS = List.of(
            "/api/v1/admin/system/maintenance-mode",
            "/api/v1/admin/system/configs",
            "/api/v1/actuator" // endpoint của Spring Boot actuator
    );

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain filterChain) throws ServletException, IOException {
        // full url: http://localhost:3000/api/v1/admin/?abc=123
        // getRequestURI: /api/v1/admin
        String requestUri = req.getRequestURI();

        if (maintenanceModeService.isMaintenanceModeEnabled()) {
            boolean isAllowedEnpoint = ALLOWED_ENDPOINTS.stream()
                    .anyMatch(requestUri::startsWith);
            if (isAllowedEnpoint) {
                // Cho phép ADMIN access maintenance endpoints
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("authentication" + authentication);
                if (authentication != null
                        && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                    filterChain.doFilter(req, res);
                    return;
                }
            }

            // Block request và trả về 503
            log.warn("MAINTENANCE MODE: Blocked request to {} from {}",
                    requestUri, req.getRemoteAddr());
            res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");

            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .success(false)
                    .message("System is under maintenance")
                    .error("The system is currently undergoing maintenance. Please try again later.")
                    .build();

            res.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        filterChain.doFilter(req, res);
    }
}
/*
 - Arrays.asList(): Tạo ra một danh sách cố định kích thước (dựa trên mảng). Không thể thêm hoặc xóa,
 nhưng có thể thay đổi phần tử bên trong.

 -List.of(): Tạo ra danh sách bất biến (immutable). Không thể thêm, xóa, sửa gì hết.

  Chuyển Java → JSON:
  writeValueAsString(Object obj)

  Chuyển JSON → Java:
  readValue(String json, Class<T> clazz)
*/
