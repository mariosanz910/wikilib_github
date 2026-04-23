package com.tfg.wikilib.repository;

import com.tfg.wikilib.model.Publicacion;
import com.tfg.wikilib.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    // Todas las publicaciones (para el catálogo público)
    List<Publicacion> findAllByOrderByFechaCreacionDesc();

    // Buscar por título (contiene, sin importar mayúsculas)
    List<Publicacion> findByTituloContainingIgnoreCaseOrderByFechaCreacionDesc(String titulo);

    // Filtrar por categoría
    List<Publicacion> findByCategoriaIdOrderByFechaCreacionDesc(Long categoriaId);

    // Publicaciones de un autor concreto (para el panel del redactor)
    List<Publicacion> findByAutorOrderByFechaCreacionDesc(Usuario autor);
}
