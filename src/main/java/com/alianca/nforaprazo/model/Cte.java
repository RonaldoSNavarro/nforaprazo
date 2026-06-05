package com.alianca.nforaprazo.model;

import com.alianca.nforaprazo.model.enums.StatusCte;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cte")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_cte", length = 50)
    private String numeroCte;

    @Column(name = "chave_acesso", length = 44)
    private String chaveAcesso;

    @Column(name = "tomador_nome", length = 255)
    private String tomadorNome;

    @Column(name = "tomador_cnpj", length = 18)
    private String tomadorCnpj;

    @Column(name = "navio", length = 255)
    private String navio;

    @Column(name = "viagem", length = 100)
    private String viagem;

    @Column(name = "porto_origem", length = 255)
    private String portoOrigem;

    @Column(name = "porto_destino", length = 255)
    private String portoDestino;

    @Column(name = "valor_carga", precision = 15, scale = 2)
    private BigDecimal valorCarga;

    @Column(name = "numero_booking", length = 100)
    private String numeroBooking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusCte status;

    @Column(name = "arquivo_pdf_path", nullable = false, length = 255)
    private String arquivoPdfPath;

    @Column(name = "nome_original_arquivo", nullable = false, length = 255)
    private String nomeOriginalArquivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_upload_id", nullable = false)
    private Usuario usuarioUpload;

    @Column(name = "data_upload", nullable = false, updatable = false)
    private LocalDateTime dataUpload;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer versao = 0;

    // --- Relacionamentos ---
    @OneToOne(mappedBy = "cte", cascade = CascadeType.ALL)
    private AutoInfracao autoInfracao;

    @OneToMany(mappedBy = "cte", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NotaFiscal> notasFiscais = new ArrayList<>();

    @OneToMany(mappedBy = "cte", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LogAlerta> alertas = new ArrayList<>();

    @OneToOne(mappedBy = "cte", cascade = CascadeType.ALL)
    private EncSemAuto encSemAuto;

    @OneToOne(mappedBy = "cte", cascade = CascadeType.ALL)
    private NotaDebito notaDebito;

    public BigDecimal getValorTotalNotas() {
        if (notasFiscais == null || notasFiscais.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return notasFiscais.stream()
                .map(NotaFiscal::getValorNota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getValorPotencialMulta() {
        return getValorTotalNotas().multiply(new BigDecimal("0.10"));
    }

    // --- Lifecycle Callbacks ---
    @PrePersist
    protected void onCreate() {
        LocalDateTime agora = LocalDateTime.now();
        dataUpload = agora;
        dataAtualizacao = agora;
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    // --- Métodos de negócio ---
    /**
     * Verifica se o porto de destino requer alerta para a equipe DESCARGA
     * (RN05). Portos monitorados: Manaus, Vila do Conde/PCM, Pecém.
     */
    public boolean isPortoMonitorado() {
        if (portoDestino == null) {
            return false;
        }
        String destino = portoDestino.toUpperCase().trim();
        return destino.contains("MANAUS")
                || destino.contains("MAO")
                || destino.contains("VILA DO CONDE")
                || destino.contains("PCM")
                || destino.contains("PECEM")
                || destino.contains("PECÉM");
    }
}
