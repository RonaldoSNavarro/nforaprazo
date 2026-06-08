package com.sistema.nforaprazo.repository;

import com.sistema.nforaprazo.model.PortoMonitorado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortoMonitoradoRepository extends JpaRepository<PortoMonitorado, UUID> {
    List<PortoMonitorado> findByAtivoTrue();
    boolean existsByNomeIgnoreCase(String nome);
    Optional<PortoMonitorado> findByNomeIgnoreCase(String nome);
}
