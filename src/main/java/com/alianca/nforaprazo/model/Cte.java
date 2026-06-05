package com.alianca.nforaprazo.model;

import com.alianca.nforaprazo.model.enums.StatusCte;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Column(name = "chave_acesso", length = 44)
    private String chaveAcesso;

    @Column(name = "valor_carga", precision = 15, scale = 2)
    private BigDecimal valorCarga;

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
    @Builder.Default
    private LocalDateTime dataUpload = LocalDateTime.now();

    @Column(name = "data_atualizacao", nullable = false)
    @Builder.Default
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer versao = 0;

    @OneToOne(mappedBy = "cte", cascade = CascadeType.ALL)
    private AutoInfracao autoInfracao;

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}
