package com.tfg.wikilib.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorito", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"entrada_id", "usuario_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "entrada_id", nullable = false)
    private Publicacion publicacion;
}
