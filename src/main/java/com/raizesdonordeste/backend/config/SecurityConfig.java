package com.raizesdonordeste.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.raizesdonordeste.backend.security.JwtAuthenticationFilter;
import com.raizesdonordeste.backend.security.ApiAccessDeniedHandler;
import com.raizesdonordeste.backend.security.ApiAuthenticationEntryPoint;
import com.raizesdonordeste.backend.service.JwtService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ApiAuthenticationEntryPoint apiAuthenticationEntryPoint,
            ApiAccessDeniedHandler apiAccessDeniedHandler) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(Customizer.withDefaults());
        http.sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(apiAuthenticationEntryPoint)
                .accessDeniedHandler(apiAccessDeniedHandler));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/clientes").permitAll()
                .requestMatchers(HttpMethod.POST, "/clientes/login").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/unidades/*/cardapio").hasAnyRole("ADMIN", "GERENTE", "CLIENTE")
                .requestMatchers(HttpMethod.GET, "/unidades/*/produtos-disponiveis").hasAnyRole("ADMIN", "GERENTE", "CLIENTE")
                .requestMatchers("/produto-unidades/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/relatorios/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/funcionarios/**").hasRole("ADMIN")
                .requestMatchers("/unidades/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/produtos/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/estoque/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/pagamentos/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/pedidos/**").hasAnyRole("ADMIN", "GERENTE", "CLIENTE")
                .requestMatchers("/fidelidade/**").hasAnyRole("ADMIN", "GERENTE", "CLIENTE")
                .requestMatchers("/clientes/**").hasAnyRole("ADMIN", "CLIENTE")
                .anyRequest().authenticated());

        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
