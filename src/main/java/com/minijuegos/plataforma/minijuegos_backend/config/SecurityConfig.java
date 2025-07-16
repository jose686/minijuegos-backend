package com.minijuegos.plataforma.minijuegos_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity // Habilita la seguridad web de Spring
public class SecurityConfig {

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
                        .permitAll() // Permite acceso a la página de login (redundante si ya está en authorizeHttpRequests, pero no hace daño)
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL para el logout
                        .logoutSuccessUrl("/") // Redirige a la raíz después de logout
                        .permitAll()
                )
                // --- INICIO DE LA SECCIÓN AGREGADA PARA RESOLVER EL ERROR DEL IFRAME ---
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions
                                        .sameOrigin() // Permite que el contenido sea incrustado por páginas del mismo origen
                                // Si por alguna razón necesitaras permitir desde cualquier origen (MENOS SEGURO):
                                // .disable()
                        )
                )
                // --- FIN DE LA SECCIÓN AGREGADA ---
                .csrf(csrf -> csrf.disable()); // Deshabilita CSRF para desarrollo, ¡habilitar y configurar para producción!
        return http.build();
    }

    // El resto de tus beans (UserDetailsService y PasswordEncoder) permanecen igual

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Define un usuario en memoria para el administrador
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("1234")) // ¡CAMBIA ESTO POR UNA CONTRASEÑA MÁS SEGURA EN PRODUCCIÓN!
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Usa BCrypt para hashear contraseñas
    }
}