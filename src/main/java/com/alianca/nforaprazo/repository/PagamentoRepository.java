package com.alianca.nforaprazo.repository;

import com.alianca.nforaprazo.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, UUID> {

    @Query("SELECT COALESCE(SUM(p.valorPago), 0) FROM Pagamento p")
    BigDecimal sumTotalPago();
}

