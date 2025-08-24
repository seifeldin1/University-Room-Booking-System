//package com.example.University.config;
//
//import com.example.University.security.jwt.JwtAuthenticationEntryPoint;
//import com.example.University.security.jwt.JwtAuthenticationFilter;
//import com.example.University.service.CustomUserDetailsService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final CustomUserDetailsService userDetailsService;
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        return authProvider;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        // Public endpoints
//                        .requestMatchers("/api/v1/auth/**").permitAll()
//                        .requestMatchers("/api/v1/health", "/api/v1/info").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/v1/buildings/**", "/api/v1/rooms/search").permitAll()
//
//                        // Student endpoints
//                        .requestMatchers(HttpMethod.GET, "/api/v1/rooms/**", "/api/v1/features/**").hasAnyRole("STUDENT", "FACULTY", "ADMIN")
//                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings").hasAnyRole("STUDENT", "FACULTY", "ADMIN")
//                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/my").hasAnyRole("STUDENT", "FACULTY", "ADMIN")
//                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/*/cancel").hasAnyRole("STUDENT", "FACULTY", "ADMIN")
//
//                        // Faculty endpoints (same as student for now, can be extended)
//                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/department").hasAnyRole("FACULTY", "ADMIN")
//
//                        // Admin endpoints
//                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/pending").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/*/approve").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/*/reject").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.POST, "/api/v1/rooms").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/v1/rooms/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/v1/rooms/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.POST, "/api/v1/buildings").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/v1/buildings/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/v1/buildings/**").hasRole("ADMIN")
//                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
//                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
//
//                        // All other requests must be authenticated
//                        .anyRequest().authenticated()
//                );
//
//        http.authenticationProvider(authenticationProvider());
//        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOriginPatterns(List.of("*"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}

package com.example.University.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
