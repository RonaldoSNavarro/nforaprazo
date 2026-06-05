package com.alianca.nforaprazo.repository;

import com.alianca.nforaprazo.model.EncSemAuto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EncSemAutoRepository extends JpaRepository<EncSemAuto, UUID> {
}
