package com.tfg.wikilib.repository;

import com.tfg.wikilib.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    @Query("SELECT c, COUNT(p) as total FROM Categoria c JOIN Publicacion p ON p.categoria = c GROUP BY c ORDER BY total DESC")
    List<Object[]> findMostPopularCategories();
}