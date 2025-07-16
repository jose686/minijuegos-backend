package com.minijuegos.plataforma.minijuegos_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.minijuegos.plataforma.minijuegos_backend.model.Minijuego;
import com.minijuegos.plataforma.minijuegos_backend.repository.MinijuegoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import com.minijuegos.plataforma.minijuegos_backend.model.Role; // Importa tu modelo Role
import com.minijuegos.plataforma.minijuegos_backend.model.User;   // Importa tu modelo User
import com.minijuegos.plataforma.minijuegos_backend.repository.RoleRepository; // Importa tu RoleRepository
import com.minijuegos.plataforma.minijuegos_backend.repository.UserRepository; // Importa tu UserRepository

import org.springframework.security.crypto.password.PasswordEncoder; // Importa PasswordEncoder
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class MinijuegosBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinijuegosBackendApplication.class, args);
	}



	// Este método se ejecutará una vez que la aplicación se inicie
	@Bean
	public CommandLineRunner demoData(MinijuegoRepository repository) {
		return args -> {
			// Solo añadir si no existe ya para evitar duplicados
			if (repository.findByNombre("Minijuego de Suma Simple").isEmpty()) {
				Minijuego sumaJuego = new Minijuego(
						"Minijuego de Suma Simple",
						"Practica sumas básicas de forma divertida.",
						"/images/default-thumbnail.png", // Asegúrate de tener esta imagen o usa una URL real
						"minijuegos/suma-simple/index.html"
				);
				repository.save(sumaJuego);
				System.out.println("Minijuego de Suma Simple añadido.");
			}

			// Puedes añadir más minijuegos de prueba aquí si quieres
			if (repository.findByNombre("Minijuego de Letras Ocultas").isEmpty()) {
				Minijuego letrasJuego = new Minijuego(
						"Minijuego de Letras Ocultas",
						"Encuentra las letras perdidas en esta sopa de letras.",
						"/images/default-thumbnail.png",
						"minijuegos/letras-ocultas/index.html"
				);
				repository.save(letrasJuego);
				System.out.println("Minijuego de Letras Ocultas añadido.");
			}
		};
	}
	// ¡NUEVO BEAN! Para inicializar el usuario administrador en la base de datos
	@Bean
	public CommandLineRunner initAdminUser(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// 1. Asegúrate de que el rol "ROLE_ADMIN" exista
			Role adminRole = roleRepository.findByName("ROLE_ADMIN")
					.orElseGet(() -> {
						Role newRole = new Role("ROLE_ADMIN");
						return roleRepository.save(newRole);
					});

			// 2. Crea un usuario administrador si no existe ya
			if (userRepository.findByUsername("admin").isEmpty()) {
				User adminUser = new User();
				adminUser.setUsername("admin");
				// Codifica la contraseña antes de guardarla
				adminUser.setPassword(passwordEncoder.encode("1234")); // ¡Cambia esta contraseña en producción!

				Set<Role> roles = new HashSet<>();
				roles.add(adminRole);
				adminUser.setRoles(roles);

				userRepository.save(adminUser);
				System.out.println("Usuario 'admin' creado en la base de datos con contraseña '1234'");
			}
		};
	}
}
