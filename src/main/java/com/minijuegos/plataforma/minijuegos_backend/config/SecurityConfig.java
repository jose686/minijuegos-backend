package com.minijuegos.plataforma.minijuegos_backend.config;

import com.minijuegos.plataforma.minijuegos_backend.service.CustomUserDetailsService; // ¡Importa tu nuevo servicio!
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Elimina estas importaciones que ya no necesitas para el usuario en memoria:
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService; // La clase UserDetailsService sí se usa, pero el bean ya no será InMemoryUserDetailsManager
// import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService; // Inyecta tu servicio personalizado

    // Constructor para inyección de dependencia de CustomUserDetailsService
    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Permite acceso público a estas rutas
                        .requestMatchers("/", "/jugar/**", "/css/**", "/images/**", "/minijuegos/**", "/admin/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Protege /admin/ y sus subrutas para el rol ADMIN
                        .anyRequest().authenticated() // Cualquier otra petición requiere autenticación
                )
                .formLogin(form -> form
                        .loginPage("/admin/login") // Especifica tu página de login personalizada
                        .defaultSuccessUrl("/admin/dashboard", true) // Redirige aquí tras login exitoso
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL para el logout
                        .logoutSuccessUrl("/") // Redirige a la raíz después de logout
                        .permitAll()
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions
                                .sameOrigin() // Permite que el contenido sea incrustado por páginas del mismo origen
                        )
                )
                .csrf(csrf -> csrf.disable()); // Deshabilita CSRF para desarrollo, ¡habilitar y configurar para producción!

        // ¡IMPORTANTE! Configura Spring Security para que use tu CustomUserDetailsService
        http.userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Mantienes este bean, ya que es el que encriptará las contraseñas
        return new BCryptPasswordEncoder();
    }

    // ¡ELIMINA O COMENTA ESTE BEAN! Ya no necesitas el usuario en memoria.
    // @Bean
    // public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    //     UserDetails admin = User.builder()
    //             .username("admin")
    //             .password(passwordEncoder.encode("1234"))
    //             .roles("ADMIN")
    //             .build();
    //     return new InMemoryUserDetailsManager(admin);
    // }
}