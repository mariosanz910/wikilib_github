package com.tfg.wikilib.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(
    name = "publicacion_leida",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "publicacion_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicacionLeida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publicacion_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Publicacion publicacion;

    @Column(name = "fecha_lectura", nullable = false)
    @Builder.Default
    private LocalDateTime fechaLectura = LocalDateTime.now();
}