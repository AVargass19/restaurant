package com.restaurant.config;

import com.restaurant.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF para poder usar Swagger
                .csrf(AbstractHttpConfigurer::disable)

                // Definir reglas de autorización para URLs
                .authorizeHttpRequests(authorize -> authorize
                        // Recursos estáticos permitidos para todos
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // Swagger / OpenAPI permitido para todos
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Páginas públicas
                        .requestMatchers("/", "/login", "/register", "/error/**").permitAll()

                        // Rutas para administradores
                        .requestMatchers("/admin/**", "/users/**", "/history/**").hasRole("ADMIN")

                        // Rutas para administradores y staff
                        .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")

                        // Permitir acceso a la gestión de mesas solo a admin y staff
                        .requestMatchers("/tables/**").hasAnyRole("ADMIN", "STAFF")

                        // Rutas específicas para reservas
                        .requestMatchers("/reservations/delete/**").hasRole("ADMIN")
                        .requestMatchers("/reservations/complete/**").hasAnyRole("ADMIN", "STAFF")

                        // Cualquier otra solicitud requiere autenticación
                        .anyRequest().authenticated()
                )

                // Configuración del formulario de login
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // Configuración de logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // Recordar usuario (remember-me)
                .rememberMe(remember -> remember
                        .key("miClaveSecretaParaRememberMe")
                        .tokenValiditySeconds(86400) // 1 día
                        .userDetailsService(userDetailsService)
                )

                // Manejo de excepciones y acceso denegado
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/error/access-denied")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }
}