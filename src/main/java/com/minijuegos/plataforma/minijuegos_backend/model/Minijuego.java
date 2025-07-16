package com.minijuegos.plataforma.minijuegos_backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.persistence.*; // Asegúrate de usar jakarta.persistence para Spring Boot 3+

@Entity // Indica que esta clase es una entidad JPA y se mapeará a una tabla de base de datos
@Table(name = "minijuegos") // Opcional: Define el nombre de la tabla en la DB
public class Minijuego {

    @Id // Marca este campo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Generación automática de IDs (autoincremento)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío.")
    @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres.")
    @Column(nullable = false, unique = true) // Esto ya estaba para la BD
    private String nombre;

    @Column(length = 500) // Límite de longitud para la descripción
    private String descripcion;

    @Column(nullable = false)
    private String urlPortada; // URL de la imagen de portada o thumbnail del juego

    @Column(nullable = false)
    private String rutaJuego; // Ruta relativa dentro de 'static/minijuegos' donde está el HTML/JS del juego

    // Constructor vacío (necesario para JPA)
    public Minijuego() {
    }

    // Constructor con todos los campos (útil para crear objetos)
    public Minijuego(String nombre, String descripcion, String urlPortada, String rutaJuego) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.urlPortada = urlPortada;
        this.rutaJuego = rutaJuego;
    }

    // --- Getters y Setters ---
    // (Puedes generarlos automáticamente con tu IDE: Alt+Insert en IntelliJ/Eclipse, o clic derecho -> Source Action -> Generate Getters and Setters en VS Code)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUrlPortada() {
        return urlPortada;
    }

    public void setUrlPortada(String urlPortada) {
        this.urlPortada = urlPortada;
    }

    public String getRutaJuego() {
        return rutaJuego;
    }

    public void setRutaJuego(String rutaJuego) {
        this.rutaJuego = rutaJuego;
    }

    @Override
    public String toString() {
        return "Minijuego{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", urlPortada='" + urlPortada + '\'' +
                ", rutaJuego='" + rutaJuego + '\'' +
                '}';
    }
}
