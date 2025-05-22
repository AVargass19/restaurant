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
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(authorize -> authorize
                        // Recursos estáticos permitidos para todos
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/img/**").permitAll()

                        // Swagger / OpenAPI permitido para todos
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Páginas públicas
                        .requestMatchers("/", "/index", "/login", "/register", "/error/**", "/menu", "/about-us").permitAll()

                        // Permitir cambios de idioma sin autenticación
                        .requestMatchers(request -> request.getParameterMap().containsKey("lang")).permitAll()

                        // Rutas específicas del perfil
                        .requestMatchers("/users/profile", "/users/profile/**").authenticated()

                        // API para verificar username (solo admin)
                        .requestMatchers("/users/api/**").hasRole("ADMIN")

                        // Rutas para administradores (gestión de usuarios)
                        .requestMatchers("/admin/**", "/users/**", "/history/**", "/usuarios/**", "/historial/**").hasRole("ADMIN")

                        // Rutas para administradores y staff
                        .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")

                        // Permitir acceso a la gestión de mesas solo a admin y staff
                        .requestMatchers("/tables/**", "/mesas/**").hasAnyRole("ADMIN", "STAFF")

                        // Excepción para la vista de mesas disponibles (accesible para todos los usuarios autenticados)
                        .requestMatchers("/tables/available").authenticated()

                        // Rutas específicas para reservas - más detalladas
                        .requestMatchers("/reservations/delete/**", "/reservas/eliminar/**").hasRole("ADMIN")
                        .requestMatchers("/reservations/complete/**", "/reservas/completar/**").hasAnyRole("ADMIN", "STAFF")

                        // Permitir a usuarios autenticados acceder a crear, ver y gestionar sus propias reservas
                        .requestMatchers("/reservations", "/reservations/create", "/reservations/edit/**", "/reservations/cancel/**").authenticated()

                        // Dashboard accesible para todos los usuarios autenticados (cada rol ve su propia versión)
                        .requestMatchers("/dashboard").authenticated()

                        // Cualquier otra solicitud requiere autenticación
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("miClaveSecretaParaRememberMe")
                        .tokenValiditySeconds(86400)
                        .userDetailsService(userDetailsService)
                )
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

    // CONFIGURACIÓN DE INTERNACIONALIZACIÓN

    /**
     * Define el solucionador de locales para i18n.
     * Utiliza SessionLocaleResolver para almacenar la preferencia de idioma en la sesión del usuario.
     * El idioma predeterminado es español (es).
     */
    @Bean
    public SessionLocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(new Locale("es"));
        return localeResolver;
    }

    /**
     * Configura el interceptor para cambio de idioma.
     * Este interceptor detecta el parámetro "lang" en las peticiones y cambia el idioma en base a él.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }

    /**
     * Configura la fuente de mensajes para i18n.
     * Carga los archivos de propiedades messages_XX.properties del classpath.
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    /**
     * Registra el interceptor de cambio de idioma en la aplicación.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}