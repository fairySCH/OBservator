package com.happy.observator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @SuppressWarnings("unused")
    @Autowired
    private UserDetailsService userDetailsService;  // Field injection

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authz) -> authz
                .requestMatchers("/signup", "/login", "/h2-console/**").permitAll()
                .requestMatchers("/profile").authenticated()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())  // Disable CSRF protection for H2 console
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))  // Allow H2 frames
            .formLogin((form) -> form
                .loginPage("/login")
                .failureUrl("/login?error=true")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll());

        return http.build();
    }
}

