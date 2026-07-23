package com.sistema.nforaprazo.repository;

import com.sistema.nforaprazo.model.AutoInfracao;
import com.sistema.nforaprazo.dto.AutoInfracaoMensalDto;
import com.sistema.nforaprazo.dto.ResponsabilidadeDto;
import com.sistema.nforaprazo.dto.ReincidenteDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AutoInfracaoRepository extends JpaRepository<AutoInfracao, UUID> {

    @Query("SELECT NEW com.sistema.nforaprazo.dto.AutoInfracaoMensalDto(year(a.dataEmissao), month(a.dataEmissao), COUNT(a), COALESCE(SUM(a.valorMulta), 0)) " +
           "FROM AutoInfracao a " +
           "WHERE a.dataEmissao IS NOT NULL " +
           "GROUP BY year(a.dataEmissao), month(a.dataEmissao) " +
           "ORDER BY year(a.dataEmissao) ASC, month(a.dataEmissao) ASC")
    List<AutoInfracaoMensalDto> findEvolucaoMensal();

    @Query("SELECT NEW com.sistema.nforaprazo.dto.ResponsabilidadeDto(a.responsavel, COUNT(a), COALESCE(SUM(a.valorMulta), 0)) " +
           "FROM AutoInfracao a " +
           "GROUP BY a.responsavel")
    List<ResponsabilidadeDto> findResponsabilidadeStats();

    @Query("SELECT NEW com.sistema.nforaprazo.dto.ReincidenteDto(c.tomadorNome, COUNT(a), COALESCE(SUM(a.valorMulta), 0)) " +
           "FROM AutoInfracao a JOIN a.cte c " +
           "GROUP BY c.tomadorNome " +
           "ORDER BY COUNT(a) DESC")
    List<ReincidenteDto> findTopTomadores(Pageable pageable);
}

