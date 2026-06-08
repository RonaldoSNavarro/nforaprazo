package com.sistema.nforaprazo.repository;

import com.sistema.nforaprazo.model.NotaFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotaFiscalRepository extends JpaRepository<NotaFiscal, UUID> {

    List<NotaFiscal> findByCteId(UUID cteId);

    /**
     * Soma o valor de todas as NFs vinculadas a um CT-e.
     * Usado para cálculo de multa (RN01: 10% do somatório).
     */
    @Query("SELECT COALESCE(SUM(nf.valorNota), 0) FROM NotaFiscal nf WHERE nf.cte.id = :cteId")
    BigDecimal somarValoresPorCteId(@Param("cteId") UUID cteId);

    long countByCteId(UUID cteId);
}
