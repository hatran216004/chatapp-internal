package com.example.librarymanagement.security.filter;

import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.repository.UserRepository;
import com.example.librarymanagement.security.util.JwtTokenProvider;
import com.example.librarymanagement.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;

    /*
     * Authentication (UsernamePasswordAuthenticationToken) — object chứa credentials (username/password)
     * trước khi xác thực; sau khi xác thực, object này chứa principal (thông tin user) và authorities.
     *
     * SecurityContextHolder — lưu Authentication cho session/luồng hiện tại (nếu ứng dụng stateful)
     * hoặc để filter tiếp theo tham chiếu.
     * */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String jwt = getJwtFromRequest(req);
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateAccessToken(jwt)) {
            Integer userId = Integer.parseInt(jwtTokenProvider.extractSubject(jwt));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(req, res);
    }

    private String getJwtFromRequest(HttpServletRequest req) {
        final String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}

















































































































