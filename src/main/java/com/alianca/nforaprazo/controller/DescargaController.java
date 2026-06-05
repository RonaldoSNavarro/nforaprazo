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
        Page<Cte> pendentes = cteRepository.findByStatusIn(
                java.util.List.of(
                        StatusCte.AGUARDANDO_DESEMBARACO,
                        StatusCte.DESEMBARACADO,
                        StatusCte.AUTO_RECEBIDO,
                        StatusCte.AGUARDANDO_PAGAMENTO
                ), 
                PageRequest.of(page, 15, Sort.by(Sort.Direction.ASC, "dataUpload")));
        
        model.addAttribute("ctes", pendentes);
        return "descarga/pendentes";
    }

    @PostMapping("/confirmar-desembaraco")
    public String confirmarDesembaraco(@RequestParam("cteId") UUID cteId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            descargaService.confirmarDesembaraco(cteId, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Desembaraço confirmado!");
        } catch (Exception e) {
            log.error("Erro", e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/descarga/pendentes";
    }

    @PostMapping("/auto-infracao")
    public String registrarAutoInfracao(@RequestParam("cteId") UUID cteId, @RequestParam("autoInfracaoPdf") MultipartFile autoInfracaoPdf, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            descargaService.registrarAutoInfracao(cteId, autoInfracaoPdf, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Auto de Infração registrado!");
        } catch (Exception e) {
            log.error("Erro", e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/descarga/pendentes";
    }

    @PostMapping("/dar")
    public String registrarDar(@RequestParam("cteId") UUID cteId, @RequestParam("darPdf") MultipartFile darPdf, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            descargaService.registrarDar(cteId, darPdf, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "DAR registrado com sucesso!");
        } catch (Exception e) {
            log.error("Erro", e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/descarga/pendentes";
    }

    @PostMapping("/comprovante")
    public String registrarComprovante(@RequestParam("cteId") UUID cteId, 
                                       @RequestParam("valorPago") BigDecimal valorPago, 
                                       @RequestParam("dataPagamento") LocalDate dataPagamento, 
                                       @RequestParam("comprovantePdf") MultipartFile comprovantePdf, 
                                       @RequestParam(value = "capaPdf", required = false) MultipartFile capaPdf, 
                                       Authentication authentication, 
                                       RedirectAttributes redirectAttributes) {
        try {
            descargaService.registrarComprovante(cteId, valorPago, dataPagamento, comprovantePdf, capaPdf, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Comprovante registrado e pagamento finalizado!");
        } catch (Exception e) {
            log.error("Erro", e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/descarga/pendentes";
    }
}
