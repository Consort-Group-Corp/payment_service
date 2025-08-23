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
import uz.consortgroup.payment_service.security.CustomAccessDeniedHandler;
import uz.consortgroup.payment_service.security.PaycomAuthFilter;
import uz.consortgroup.payment_service.service.util.AuthEntryPointJwt;
import uz.consortgroup.payment_service.service.util.AuthTokenFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final PaycomAuthFilter paycomAuthFilter;
    private final ClickAuthFilter clickAuthFilter;
    private final AuthTokenFilter authTokenFilter;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // провайдерские колбэки/ручки — пускаем без JWT, валидируются их фильтрами
                        .requestMatchers(HttpMethod.POST, "/api/v1/paycom/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/click/**").permitAll()

                        // бизнес-ручки
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/v1/orders/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/refunds/**").authenticated()

                        // internal — не открываем наружу
                        .requestMatchers("/internal/**").authenticated()

                        .anyRequest().denyAll()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // порядок: paycom -> click -> jwt -> UsernamePasswordAuthenticationFilter
                .addFilterBefore(paycomAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(clickAuthFilter,  PaycomAuthFilter.class)
                .addFilterBefore(authTokenFilter,  ClickAuthFilter.class);

        return http.build();
    }
}
