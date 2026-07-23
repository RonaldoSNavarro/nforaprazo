package com.sistema.nforaprazo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ForcarSenhaFiltro forcarSenhaFiltro;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/css/**", "/js/**", "/fonts/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/alterar-senha").authenticated()
                .requestMatchers("/admin/**").hasAnyRole("ADMINISTRADOR", "GESTAO")
                .requestMatchers("/actuator/**").hasAnyRole("ADMINISTRADOR", "GESTAO")
                .requestMatchers("/faturamento/**").hasAnyRole("FATURAMENTO", "ADMINISTRADOR")
                .requestMatchers("/descarga/**").hasAnyRole("DESCARGA", "ADMINISTRADOR")
                .requestMatchers("/docs-fiscal/**").hasAnyRole("DOCS_FISCAL", "ADMINISTRADOR")
                .requestMatchers("/dashboard/**", "/relatorios").hasAnyRole("GESTAO", "ADMINISTRADOR")
                .requestMatchers("/cte/**").hasAnyRole("FATURAMENTO", "DESCARGA", "DOCS_FISCAL", "GESTAO", "ADMINISTRADOR")
                .anyRequest().authenticated()
            )
            .addFilterAfter(forcarSenhaFiltro, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .usernameParameter("email")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/acesso-negado")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
