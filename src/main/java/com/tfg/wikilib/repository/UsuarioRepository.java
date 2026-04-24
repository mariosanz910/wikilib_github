package com.tfg.wikilib.repository;

import com.tfg.wikilib.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByEmail(String email);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByEmail(String email);

    @Query("SELECT u, COUNT(p) as total FROM Usuario u JOIN Publicacion p ON p.autor = u GROUP BY u ORDER BY total DESC")
    List<Object[]> findMostActiveWriters();
}