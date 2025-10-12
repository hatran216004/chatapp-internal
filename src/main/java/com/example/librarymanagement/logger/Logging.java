package com.example.librarymanagement.logger;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class Logging extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Instant start = Instant.now();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";

        filterChain.doFilter(request, response);

        long durationMs = Duration.between(start, Instant.now()).toMillis();
        int status = response.getStatus();

        String colorStatus = switch (status / 100) {
            case 2 -> "\u001B[32m" + status + "\u001B[0m";
            case 4 -> "\u001B[33m" + status + "\u001B[0m";
            case 5 -> "\u001B[31m" + status + "\u001B[0m";
            default -> String.valueOf(status);
        };

        log.info("{} {}{} {} {}ms", method, uri, query, colorStatus, durationMs);
    }
}
