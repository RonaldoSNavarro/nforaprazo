package com.sistema.nforaprazo.repository;

import com.sistema.nforaprazo.model.EncSemAuto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface EncSemAutoRepository extends JpaRepository<EncSemAuto, UUID> {

    @Query("SELECT COALESCE(SUM(e.valorPotencialMulta), 0) FROM EncSemAuto e")
    BigDecimal sumTotalEvitado();
}

