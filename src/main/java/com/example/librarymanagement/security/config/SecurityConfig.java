package com.example.librarymanagement.security.config;

import com.example.librarymanagement.exception.AccessDeniedHandler;
import com.example.librarymanagement.exception.AuthEntryPointJwt;
import com.example.librarymanagement.security.filter.JwtAuthenticationFilter;
import com.example.librarymanagement.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthEntryPointJwt authEntryPointJwt;
    private final AccessDeniedHandler accessDeniedHandler;

    private static final String[] WHITE_LIST_URL = {"/auth/**"};

    // dùng để so khớp mật khẩu raw do client gửi với mật khẩu đã mã hóa lưu ở DB
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Check password
    @Bean
    public AuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .cors(cors -> {
                })
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF vì app dùng JWT
                .sessionManagement(sm
                        -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không dùng session
                // Gắn handler cho lỗi 401/403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPointJwt)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authenticationProvider(daoAuthenticationProvider(userDetailsService, passwordEncoder()))
                // Quy định endpoint nào cần login
                .authorizeHttpRequests(auth
                        -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                /*
                    Thêm filter kiểm tra JWT và đặt trước UsernamePasswordAuthenticationFilter
                    * UsernamePasswordAuthenticationFilter là filter mặc định của Spring để xử lý form login (email + password).
                    Bạn dùng JWT → không qua form login, nên phải xác thực JWT sớm hơn.
                    Nếu JWT hợp lệ → set Authentication vào SecurityContextHolder
                    (để các filter sau — kể cả Authorization — nhận diện được user)
                */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Build ra chuỗi filter hoàn chỉnh
    }
}
