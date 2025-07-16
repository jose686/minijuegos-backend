package com.minijuegos.plataforma.minijuegos_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles") // Nombre de la tabla en la DB
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Ejemplo: "ROLE_ADMIN", "ROLE_USER"

    // --- Constructores ---
    public Role() {}
    public Role(String name) { this.name = name; }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}