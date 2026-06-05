package com.alianca.nforaprazo.repository;

import com.alianca.nforaprazo.model.NotaDebito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotaDebitoRepository extends JpaRepository<NotaDebito, UUID> {
}
