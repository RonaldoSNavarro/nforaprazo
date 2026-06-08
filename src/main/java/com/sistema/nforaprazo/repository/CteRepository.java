package com.sistema.nforaprazo.repository;

import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.model.enums.StatusCte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CteRepository extends JpaRepository<Cte, UUID> {
    
    Page<Cte> findByStatus(StatusCte status, Pageable pageable);
    Page<Cte> findByStatusIn(List<StatusCte> statuses, Pageable pageable);
    
    boolean existsByChaveAcesso(String chaveAcesso);

    @Query("SELECT COALESCE(SUM(nf.valorNota), 0) * 0.10 FROM Cte c JOIN c.notasFiscais nf WHERE c.status IN :statuses")
    BigDecimal sumValorPotencialMultaByStatusIn(@Param("statuses") List<StatusCte> statuses);

    @Query("SELECT DISTINCT c FROM Cte c " +
           "LEFT JOIN FETCH c.notasFiscais " +
           "LEFT JOIN FETCH c.autoInfracao ai " +
           "LEFT JOIN FETCH ai.pagamento p " +
           "WHERE (:portoDestino IS NULL OR :portoDestino = '' OR UPPER(c.portoDestino) LIKE UPPER(CONCAT('%', :portoDestino, '%'))) " +
           "AND c.dataUpload >= :dataInicio " +
           "AND c.dataUpload <= :dataFim " +
           "ORDER BY c.dataUpload DESC")
    List<Cte> findCtesReport(@Param("portoDestino") String portoDestino, 
                             @Param("dataInicio") LocalDateTime dataInicio, 
                             @Param("dataFim") LocalDateTime dataFim);
}

