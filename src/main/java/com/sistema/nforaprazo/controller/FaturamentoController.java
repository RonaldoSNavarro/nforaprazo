package com.sistema.nforaprazo.controller;

import com.sistema.nforaprazo.dto.NotaDebitoRequest;
import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.model.enums.StatusCte;
import com.sistema.nforaprazo.repository.CteRepository;
import com.sistema.nforaprazo.service.FaturamentoService;
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
@RequestMapping("/faturamento")
@RequiredArgsConstructor
@Slf4j
public class FaturamentoController {

    private final CteRepository cteRepository;
    private final FaturamentoService faturamentoService;

    @GetMapping("/nota-debito")
    public String listarNotaDebitoPendentes(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Cte> ctes = cteRepository.findByStatusIn(
                List.of(StatusCte.PAGO),
                PageRequest.of(page, 15, Sort.by(Sort.Direction.DESC, "dataUpload"))
        );
        model.addAttribute("ctes", ctes);
        return "faturamento/nota-debito";
    }

    @PostMapping("/nota-debito")
    public String emitirNotaDebito(@Valid @ModelAttribute NotaDebitoRequest request,
                                   BindingResult bindingResult,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String erroMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("erro", "Erro de validação: " + erroMsg);
            return "redirect:/faturamento/nota-debito";
        }

        try {
            faturamentoService.emitirNotaDebito(request, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Nota de Débito registrada e processo encerrado com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao emitir nota de débito para CT-e {}", request.getCteId(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/faturamento/nota-debito";
    }

    @PostMapping("/encerrar-custo-absorvido")
    public String encerrarCustoAbsorvido(@RequestParam("cteId") UUID cteId,
                                         Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        try {
            faturamentoService.encerrarCustoAbsorvido(cteId, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Processo encerrado com sucesso (Custo Absorvido pela Sistema).");
        } catch (Exception e) {
            log.error("Erro ao encerrar custo absorvido para CT-e {}", cteId, e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/faturamento/nota-debito";
    }
}
