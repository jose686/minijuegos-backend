
package com.minijuegos.plataforma.minijuegos_backend.model;

import jakarta.persistence.*;
import java.util.Set; // Para los roles

@Entity
@Table(name = "users") // Nombre de la tabla en la DB
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Aquí se guardará la contraseña encriptada

    // Un usuario puede tener varios roles (ADMIN, USER, etc.)
    @ManyToMany(fetch = FetchType.EAGER) // Cargar roles junto con el usuario
    @JoinTable(
            name = "user_roles", // Tabla intermedia para la relación ManyToMany
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    // --- Constructores ---
    public User() {}
    public User(String username, String password, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}