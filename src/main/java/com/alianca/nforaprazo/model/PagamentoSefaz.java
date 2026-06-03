package com.alianca.nforaprazo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagamento_sefaz")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoSefaz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cte_id", nullable = false, unique = true)
    private Cte cte;

    @Column(name = "valor_multa", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorMulta;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @Column(name = "path_dar_pdf", nullable = false, length = 255)
    private String pathDarPdf;

    @Column(name = "path_comprovante_pdf", nullable = false, length = 255)
    private String pathComprovantePdf;

    @Column(name = "path_capa_pdf", length = 255)
    private String pathCapaPdf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id", nullable = false)
    private Usuario usuarioRegistro;

    @Column(name = "data_registro", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dataRegistro = LocalDateTime.now();
}
