package com.tfg.wikilib.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "valoracion", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"entrada_id", "usuario_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoValoracion tipo;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "entrada_id", nullable = false)
    private Publicacion publicacion;
}