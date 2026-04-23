package com.tfg.wikilib.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entrada")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "contenido", columnDefinition = "LONGTEXT", nullable = false)
    private String texto;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPublicacion estado = EstadoPublicacion.BORRADOR;

    @Column(nullable = false)
    @Builder.Default
    private int valoracion = 0;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario autor;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    public enum EstadoPublicacion {
        BORRADOR, PUBLICADO
    }
}
