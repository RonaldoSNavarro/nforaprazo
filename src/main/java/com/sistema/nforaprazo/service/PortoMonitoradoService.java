package com.sistema.nforaprazo.service;

import com.sistema.nforaprazo.annotation.Auditable;
import com.sistema.nforaprazo.dto.PortoMonitoradoRequest;
import com.sistema.nforaprazo.dto.PortoMonitoradoResponse;
import com.sistema.nforaprazo.model.PortoMonitorado;
import com.sistema.nforaprazo.repository.PortoMonitoradoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortoMonitoradoService {

    private final PortoMonitoradoRepository portoMonitoradoRepository;

    @Transactional(readOnly = true)
    public List<PortoMonitoradoResponse> listarTodos() {
        log.info("Listando todos os portos monitorados");
        return portoMonitoradoRepository.findAll(Sort.by(Sort.Direction.ASC, "nome")).stream()
                .map(this::mapearParaResponse)
                .toList();
    }

    @Transactional
    @Auditable(acao = "Cadastro de Porto Monitorado")
    public void cadastrarPorto(PortoMonitoradoRequest request, String emailCriador) {
        String nomeNormalizado = request.getNome().trim().toUpperCase();
        log.info("Cadastrando porto monitorado: '{}' por {}", nomeNormalizado, emailCriador);

        if (portoMonitoradoRepository.existsByNomeIgnoreCase(nomeNormalizado)) {
            throw new IllegalArgumentException("Porto já cadastrado: " + nomeNormalizado);
        }

        PortoMonitorado porto = PortoMonitorado.builder()
                .nome(nomeNormalizado)
                .ativo(request.getAtivo() != null ? request.getAtivo() : true)
                .criadoPor(emailCriador)
                .build();

        portoMonitoradoRepository.save(porto);
    }

    @Transactional
    @Auditable(acao = "Edição de Porto Monitorado")
    public void editarPorto(PortoMonitoradoRequest request) {
        log.info("Editando porto monitorado ID: {}", request.getId());
        PortoMonitorado porto = portoMonitoradoRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Porto não encontrado"));

        String nomeNormalizado = request.getNome().trim().toUpperCase();
        if (!porto.getNome().equalsIgnoreCase(nomeNormalizado) && portoMonitoradoRepository.existsByNomeIgnoreCase(nomeNormalizado)) {
            throw new IllegalArgumentException("Porto já cadastrado com este nome: " + nomeNormalizado);
        }

        porto.setNome(nomeNormalizado);
        if (request.getAtivo() != null) {
            porto.setAtivo(request.getAtivo());
        }
        portoMonitoradoRepository.save(porto);
    }

    @Transactional
    @Auditable(acao = "Toggle Status de Porto Monitorado")
    public void alterarStatus(UUID id, boolean ativo) {
        log.info("Alterando status do porto monitorado ID: {} para {}", id, ativo);
        PortoMonitorado porto = portoMonitoradoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Porto não encontrado"));
        porto.setAtivo(ativo);
        portoMonitoradoRepository.save(porto);
    }

    @Transactional
    @Auditable(acao = "Exclusão de Porto Monitorado")
    public void excluirPorto(UUID id) {
        log.info("Excluindo porto monitorado ID: {}", id);
        if (!portoMonitoradoRepository.existsById(id)) {
            throw new IllegalArgumentException("Porto não encontrado");
        }
        portoMonitoradoRepository.deleteById(id);
    }

    private PortoMonitoradoResponse mapearParaResponse(PortoMonitorado porto) {
        return PortoMonitoradoResponse.builder()
                .id(porto.getId())
                .nome(porto.getNome())
                .ativo(porto.isAtivo())
                .criadoPor(porto.getCriadoPor())
                .dataCriacao(porto.getDataCriacao())
                .build();
    }
}
