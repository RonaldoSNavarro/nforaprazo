package com.alianca.nforaprazo.repository;

import com.alianca.nforaprazo.model.LogAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LogAlertaRepository extends JpaRepository<LogAlerta, UUID> {

    List<LogAlerta> findByCteIdOrderByDataEnvioDesc(UUID cteId);
}
