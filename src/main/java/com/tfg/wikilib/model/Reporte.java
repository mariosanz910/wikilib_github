package com.tfg.wikilib.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Column(nullable = false)
    @Builder.Default
    private boolean resuelto = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaReporte = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // Quien reporta

    @ManyToOne
    @JoinColumn(name = "entrada_id", nullable = false)
    private Publicacion publicacion; // Publicación reportada
}
