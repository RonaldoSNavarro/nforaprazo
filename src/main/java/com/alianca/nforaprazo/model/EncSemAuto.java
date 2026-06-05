package com.alianca.nforaprazo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "enc_sem_auto")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncSemAuto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cte_id", nullable = false, unique = true)
    private Cte cte;

    @Column(name = "valor_potencial_multa", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPotencialMulta;

    @Column(name = "justificativa", nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "data_registro", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dataRegistro = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
