package com.alianca.nforaprazo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nota_debito")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaDebito {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cte_id", nullable = false, unique = true)
    private Cte cte;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagamento_id", nullable = false, unique = true)
    private Pagamento pagamento;

    @Column(name = "numero_nota_debito", nullable = false, length = 100)
    private String numeroNotaDebito;

    @Column(name = "valor_nota_debito", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorNotaDebito;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "arquivo_pdf", nullable = false, length = 255)
    private String arquivoPdf;

    @Column(name = "data_envio_cliente")
    private LocalDateTime dataEnvioCliente;

    @Column(name = "data_registro", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dataRegistro = LocalDateTime.now();
}
