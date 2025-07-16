package com.minijuegos.plataforma.minijuegos_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.minijuegos.plataforma.minijuegos_backend.model.Minijuego;
import com.minijuegos.plataforma.minijuegos_backend.repository.MinijuegoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

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
}
