package com.minijuegos.plataforma.minijuegos_backend.controller;

import com.minijuegos.plataforma.minijuegos_backend.model.Minijuego;
import com.minijuegos.plataforma.minijuegos_backend.repository.MinijuegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller // Indica que esta clase es un controlador de Spring MVC
@RequestMapping("/") // Mapea todas las URLs relativas a la raíz del sitio
public class MinijuegoController {

    @Autowired // Inyecta la dependencia del repositorio de Minijuegos
    private MinijuegoRepository minijuegoRepository;

    // Maneja la petición para la página de inicio (listado de minijuegos)
    @GetMapping
    public String listarMinijuegos(Model model) {
        List<Minijuego> minijuegos = minijuegoRepository.findAll(); // Obtiene todos los minijuegos de la DB
        model.addAttribute("minijuegos", minijuegos); // Añade la lista de minijuegos al modelo para la vista
        return "public/index"; // Retorna el nombre de la plantilla Thymeleaf (src/main/resources/templates/public/index.html)
    }

    // Maneja la petición para ver un minijuego específico
    @GetMapping("/jugar/{id}")
    public String jugarMinijuego(@PathVariable Long id, Model model) {
        Optional<Minijuego> minijuegoOptional = minijuegoRepository.findById(id);

        if (minijuegoOptional.isPresent()) {
            Minijuego minijuego = minijuegoOptional.get();
            model.addAttribute("minijuego", minijuego);
            // La plantilla game.html necesitará la rutaJuego para incrustar el contenido
            return "public/game"; // Retorna el nombre de la plantilla Thymeleaf (src/main/resources/templates/public/game.html)
        } else {
            // Manejar caso donde el minijuego no se encuentra (ej. mostrar un error 404 o redirigir a la lista)
            return "redirect:/"; // Por ahora, redirigimos a la página de inicio
        }
    }
}