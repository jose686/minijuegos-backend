package com.minijuegos.plataforma.minijuegos_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/admin/login")
    public String login() {
        return "admin/login"; // Retorna la plantilla src/main/resources/templates/admin/login.html
    }
}