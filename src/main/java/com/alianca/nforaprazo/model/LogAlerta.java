package com.alianca.nforaprazo.model;

import com.alianca.nforaprazo.model.enums.StatusEnvio;
import com.alianca.nforaprazo.model.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "log_alerta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cte_id", nullable = false)
    private Cte cte;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false, length = 50)
    private TipoAlerta tipoAlerta;

    @Column(name = "destinatario", nullable = false, length = 255)
    private String destinatario;

    @Column(name = "assunto", length = 500)
    private String assunto;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_envio", nullable = false, length = 20)
    @Builder.Default
    private StatusEnvio statusEnvio = StatusEnvio.ENVIADO;

    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    @PrePersist
    protected void onCreate() {
        dataEnvio = LocalDateTime.now();
    }
}
