package com.alianca.nforaprazo.repository;

import com.alianca.nforaprazo.model.PagamentoSefaz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PagamentoSefazRepository extends JpaRepository<PagamentoSefaz, UUID> {
}
