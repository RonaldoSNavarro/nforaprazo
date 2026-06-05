package com.alianca.nforaprazo.repository;

import com.alianca.nforaprazo.model.Cte;
import com.alianca.nforaprazo.model.enums.StatusCte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CteRepository extends JpaRepository<Cte, UUID> {
    
    Page<Cte> findByStatus(StatusCte status, Pageable pageable);
    Page<Cte> findByStatusIn(java.util.List<StatusCte> statuses, Pageable pageable);
    
    boolean existsByChaveAcesso(String chaveAcesso);
}
