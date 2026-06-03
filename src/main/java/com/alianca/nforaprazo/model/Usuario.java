package com.alianca.nforaprazo.model;

import com.alianca.nforaprazo.model.enums.PerfilUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
}
