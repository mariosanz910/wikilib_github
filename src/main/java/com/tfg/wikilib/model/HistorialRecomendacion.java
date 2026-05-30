package com.tfg.wikilib.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "historial_recomendacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialRecomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "preferencia", nullable = false, length = 500)
    private String preferencia;

    @Column(name = "respuesta", columnDefinition = "TEXT", nullable = false)
    private String respuesta;

    @Column(name = "fecha_busqueda", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaBusqueda = LocalDateTime.now();
}