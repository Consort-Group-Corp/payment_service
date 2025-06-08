package uz.consortgroup.payment_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uz.consortgroup.payment_service.security.ClickAuthFilter;
import uz.consortgroup.payment_service.security.PaycomAuthFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final PaycomAuthFilter paycomAuthFilter;
    private final ClickAuthFilter clickAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/paycom/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/click/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders").permitAll()
                        .anyRequest().permitAll()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(paycomAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(clickAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
