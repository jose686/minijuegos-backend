package com.minijuegos.plataforma.minijuegos_backend.controller;

import com.minijuegos.plataforma.minijuegos_backend.model.Role;
import com.minijuegos.plataforma.minijuegos_backend.model.User;
import com.minijuegos.plataforma.minijuegos_backend.repository.RoleRepository;
import com.minijuegos.plataforma.minijuegos_backend.repository.UserRepository;
import jakarta.validation.Valid; // Necesario para @Valid
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Para encriptar la contraseña
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin/users") // Todas las URLs de este controlador comenzarán con /admin/users
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository; // Necesario para gestionar los roles

    @Autowired
    private PasswordEncoder passwordEncoder; // Para encriptar las contraseñas

    // Muestra la lista de usuarios
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/list-users"; // Nueva plantilla
    }

    // Muestra el formulario para añadir un nuevo usuario
    @GetMapping("/new")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleRepository.findAll()); // Pasa todos los roles disponibles
        return "admin/user-form"; // Nueva plantilla
    }

    // Muestra el formulario para editar un usuario existente
    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            model.addAttribute("allRoles", roleRepository.findAll());
            return "admin/user-form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
            return "redirect:/admin/users";
        }
    }

    // Procesa el envío del formulario para guardar o actualizar un usuario
    @PostMapping({"/save", "/save/{id}"})
    public String saveUser(@Valid @ModelAttribute User user,
                           BindingResult bindingResult,
                           @RequestParam(value = "roleIds", required = false) List<Long> roleIds, // IDs de los roles seleccionados
                           RedirectAttributes redirectAttributes,
                           Model model) {

        // Si hay errores de validación (por ejemplo, nombre de usuario vacío)
        if (bindingResult.hasErrors()) {
            // Si el ID es nulo, significa que es un usuario nuevo, o un error en la edición
            // Debemos volver a cargar los roles para que el formulario se muestre correctamente
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el usuario. Por favor, revisa los campos.");
            return "admin/user-form"; // Retorna al formulario para mostrar los errores
        }

        // Comprobar si el username ya existe para un NUEVO usuario
        if (user.getId() == null && userRepository.findByUsername(user.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "error.user", "El nombre de usuario ya existe.");
            model.addAttribute("allRoles", roleRepository.findAll()); // Asegúrate de recargar los roles para el formulario
            return "admin/user-form";
        }

        // Manejo de roles
        Set<Role> selectedRoles = new HashSet<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            roleRepository.findAllById(roleIds).forEach(selectedRoles::add);
        }
        user.setRoles(selectedRoles);

        // Manejo de la contraseña: encriptar solo si se proporciona una nueva
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // Si la contraseña está vacía en una edición, mantener la antigua
            if (user.getId() != null) {
                userRepository.findById(user.getId()).ifPresent(existingUser -> user.setPassword(existingUser.getPassword()));
            }
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario guardado correctamente.");
        return "redirect:/admin/users";
    }


    // Eliminar un usuario
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Protección: No permitir eliminar al último administrador o al usuario logueado
        // Puedes añadir lógica más robusta aquí. Por ahora, una simple comprobación.
        if (userRepository.count() == 1) { // Si es el único usuario
            redirectAttributes.addFlashAttribute("errorMessage", "No puedes eliminar el único usuario del sistema.");
            return "redirect:/admin/users";
        }
        // Puedes añadir aquí una comprobación para no borrar al usuario actualmente logueado si lo deseas.

        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
        }
        return "redirect:/admin/users";
    }
}