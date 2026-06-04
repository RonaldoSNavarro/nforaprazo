package com.alianca.nforaprazo.controller;

import com.alianca.nforaprazo.model.Cte;
import com.alianca.nforaprazo.model.enums.StatusCte;
import com.alianca.nforaprazo.repository.CteRepository;
import com.alianca.nforaprazo.service.DescargaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/descarga")
@RequiredArgsConstructor
@Slf4j
public class DescargaController {

    private final CteRepository cteRepository;
    private final DescargaService descargaService;

    @GetMapping("/pendentes")
    public String listarPendentes(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Cte> pendentes = cteRepository.findByStatus(
                StatusCte.PENDENTE_DESCARGA, 
                PageRequest.of(page, 15, Sort.by(Sort.Direction.ASC, "dataUpload")));
        
        model.addAttribute("ctes", pendentes);
        return "descarga/pendentes";
    }

    @GetMapping("/pagamento/{id}")
    public String exibirFormPagamento(@PathVariable UUID id, Model model) {
        Cte cte = cteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CT-e não encontrado"));
                
        if (cte.getStatus() != StatusCte.PENDENTE_DESCARGA) {
            return "redirect:/descarga/pendentes";
        }
        
        model.addAttribute("cte", cte);
        return "descarga/pagamento";
    }

    @PostMapping("/pagamento")
    public String registrarPagamento(@RequestParam("cteId") UUID cteId,
                                     @RequestParam("valorMulta") BigDecimal valorMulta,
                                     @RequestParam("dataPagamento") LocalDate dataPagamento,
                                     @RequestParam("darPdf") MultipartFile darPdf,
                                     @RequestParam("comprovantePdf") MultipartFile comprovantePdf,
                                     @RequestParam("autoInfracaoPdf") MultipartFile autoInfracaoPdf,
                                     @RequestParam(value = "capaPdf", required = false) MultipartFile capaPdf,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        try {
            descargaService.registrarPagamento(cteId, valorMulta, dataPagamento, darPdf, comprovantePdf, autoInfracaoPdf, capaPdf, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Pagamento SEFAZ registrado e CT-e desembaraçado com sucesso!");
            return "redirect:/descarga/pendentes";
        } catch (Exception e) {
            log.error("Erro ao registrar pagamento: ", e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao registrar pagamento: " + e.getMessage());
            return "redirect:/descarga/pagamento/" + cteId;
        }
    }
}
