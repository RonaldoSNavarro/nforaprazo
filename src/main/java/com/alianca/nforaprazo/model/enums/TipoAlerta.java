package com.alianca.nforaprazo.model.enums;

/**
 * Tipos de alerta enviados pelo sistema.
 * Cada tipo corresponde a uma transição de status do CT-e que dispara notificação.
 */
public enum TipoAlerta {
    /** CT-e registrado com porto monitorado → alerta para DESCARGA */
    NOVO_CTE_PORTO_MONITORADO,

    /** Auto de infração registrado → alerta para DOCS_FISCAL */
    AUTO_INFRACAO_RECEBIDO,

    /** Investigação concluída → alerta para DESCARGA pagar */
    INVESTIGACAO_CONCLUIDA,

    /** Pagamento confirmado → alerta para FATURAMENTO */
    PAGAMENTO_CONFIRMADO,

    /** Encerrado com auto → notificação geral */
    ENCERRADO_COM_AUTO,

    /** Encerrado sem auto → notificação geral */
    ENCERRADO_SEM_AUTO,

    /** Envio de senha provisória de novo usuário */
    SENHA_PROVISORIA
}
