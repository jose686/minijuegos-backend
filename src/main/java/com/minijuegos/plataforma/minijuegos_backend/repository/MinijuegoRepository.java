package com.minijuegos.plataforma.minijuegos_backend.repository;



import com.minijuegos.plataforma.minijuegos_backend.model.Minijuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Indica que esta interfaz es un componente de repositorio de Spring
public interface MinijuegoRepository extends JpaRepository<Minijuego, Long> {
    Optional<Minijuego> findByNombre(String nombre);
}