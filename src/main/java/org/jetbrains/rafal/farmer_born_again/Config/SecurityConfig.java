package org.jetbrains.rafal.farmer_born_again.Config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebMvcConfigurationSupport {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, PlayerAuthenticationFilter playerAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws/**", "/app/**")
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/leave", "/js/**", "/css/**", "/images/**").permitAll()
                        .requestMatchers("/waiting").permitAll()
                        .requestMatchers("/farm/**").authenticated()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/")
                        .logoutSuccessUrl("/?logout")
                        .permitAll()
                )
                .addFilterBefore(playerAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .maximumSessions(1)

                );


        return http.build();
    }
}


