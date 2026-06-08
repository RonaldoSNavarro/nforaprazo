package com.sistema.nforaprazo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nota_fiscal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cte_id", nullable = false)
    private Cte cte;

    @Column(name = "numero_nota", nullable = false, length = 50)
    private String numeroNota;

    @Column(name = "valor_nota", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorNota;

    @Column(name = "data_emissao_nf")
    private LocalDate dataEmissaoNf;

    @Column(name = "data_registro", nullable = false, updatable = false)
    private LocalDateTime dataRegistro;

    @PrePersist
    protected void onCreate() {
        dataRegistro = LocalDateTime.now();
    }
}
