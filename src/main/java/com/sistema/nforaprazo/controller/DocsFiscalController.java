package com.sistema.nforaprazo.controller;

import com.sistema.nforaprazo.dto.InvestigacaoRequest;
import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.model.enums.Responsavel;
import com.sistema.nforaprazo.model.enums.StatusCte;
import com.sistema.nforaprazo.repository.CteRepository;
import com.sistema.nforaprazo.service.DocsFiscalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/docs-fiscal")
@RequiredArgsConstructor
@Slf4j
public class DocsFiscalController {

    private final CteRepository cteRepository;
    private final DocsFiscalService docsFiscalService;

    @GetMapping("/pendentes")
    public String listarPendentes(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Cte> pendentes = cteRepository.findByStatusIn(
                List.of(StatusCte.AUTO_RECEBIDO, StatusCte.EM_INVESTIGACAO),
                PageRequest.of(page, 15, Sort.by(Sort.Direction.ASC, "dataUpload"))
        );

        model.addAttribute("ctes", pendentes);
        model.addAttribute("responsaveis", List.of(Responsavel.EMPRESA_INTERNO, Responsavel.SISTEMA, Responsavel.CLIENTE));
        return "docs-fiscal/pendentes";
    }

    @PostMapping("/iniciar-investigacao")
    public String iniciarInvestigacao(@RequestParam("cteId") UUID cteId, 
                                     Authentication authentication, 
                                     RedirectAttributes redirectAttributes) {
        try {
            docsFiscalService.iniciarInvestigacao(cteId, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Investigação iniciada com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao iniciar investigação do CT-e {}", cteId, e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/docs-fiscal/pendentes";
    }

    @PostMapping("/concluir-investigacao")
    public String concluirInvestigacao(@Valid @ModelAttribute InvestigacaoRequest request,
                                       BindingResult bindingResult,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String erroMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("erro", "Erro de validação: " + erroMsg);
            return "redirect:/docs-fiscal/pendentes";
        }

        try {
            docsFiscalService.concluirInvestigacao(request, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Investigação concluída com sucesso! CT-e liberado para pagamento.");
        } catch (Exception e) {
            log.error("Erro ao concluir investigação do CT-e {}", request.getCteId(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/docs-fiscal/pendentes";
    }
}
