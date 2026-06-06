package com.alianca.nforaprazo.repository;

import com.alianca.nforaprazo.model.AutoInfracao;
import com.alianca.nforaprazo.dto.AutoInfracaoMensalProjection;
import com.alianca.nforaprazo.dto.ResponsabilidadeProjection;
import com.alianca.nforaprazo.dto.ReincidenteProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AutoInfracaoRepository extends JpaRepository<AutoInfracao, UUID> {

    @Query("SELECT year(a.dataEmissao) as ano, month(a.dataEmissao) as mes, COUNT(a) as quantidade, SUM(a.valorMulta) as valorTotal " +
           "FROM AutoInfracao a " +
           "WHERE a.dataEmissao IS NOT NULL " +
           "GROUP BY year(a.dataEmissao), month(a.dataEmissao) " +
           "ORDER BY ano ASC, mes ASC")
    List<AutoInfracaoMensalProjection> findEvolucaoMensal();

    @Query("SELECT a.responsavel as responsavel, COUNT(a) as quantidade, SUM(a.valorMulta) as valorTotal " +
           "FROM AutoInfracao a " +
           "GROUP BY a.responsavel")
    List<ResponsabilidadeProjection> findResponsabilidadeStats();

    @Query("SELECT c.tomadorNome as tomadorNome, COUNT(a) as quantidade, SUM(a.valorMulta) as valorTotal " +
           "FROM AutoInfracao a JOIN a.cte c " +
           "GROUP BY c.tomadorNome " +
           "ORDER BY COUNT(a) DESC, SUM(a.valorMulta) DESC")
    List<ReincidenteProjection> findTopTomadores(Pageable pageable);
}

