package com.sistema.nforaprazo.repository;

import com.sistema.nforaprazo.model.LogAtividade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LogAtividadeRepository extends JpaRepository<LogAtividade, UUID> {
    
    Page<LogAtividade> findAllByOrderByDataCriacaoDesc(Pageable pageable);
    
    Page<LogAtividade> findByUsuarioEmailContainingIgnoreCaseOrderByDataCriacaoDesc(String usuarioEmail, Pageable pageable);
}
