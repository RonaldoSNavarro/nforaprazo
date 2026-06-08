package com.sistema.nforaprazo.model;

import com.sistema.nforaprazo.model.enums.Responsavel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auto_infracao")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoInfracao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cte_id", nullable = false, unique = true)
    private Cte cte;

    @Column(name = "numero_auto", length = 100)
    private String numeroAuto;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "valor_multa", precision = 15, scale = 2)
    private BigDecimal valorMulta;

    @Column(name = "arquivo_pdf_auto", length = 255)
    private String arquivoPdfAuto;

    @Enumerated(EnumType.STRING)
    @Column(name = "responsavel", nullable = false, length = 30)
    @Builder.Default
    private Responsavel responsavel = Responsavel.PENDENTE;

    @Column(name = "motivo_erro", columnDefinition = "TEXT")
    private String motivoErro;

    @Column(name = "numero_ticket", length = 100)
    private String numeroTicket;

    @Column(name = "arquivo_ticket", length = 255)
    private String arquivoTicket;

    @Column(name = "data_registro", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dataRegistro = LocalDateTime.now();

    @OneToOne(mappedBy = "autoInfracao", cascade = CascadeType.ALL)
    private Pagamento pagamento;
}
