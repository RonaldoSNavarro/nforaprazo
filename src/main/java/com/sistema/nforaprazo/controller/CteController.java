package com.sistema.nforaprazo.controller;

import com.sistema.nforaprazo.dto.CteUploadRequest;
import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.service.CteService;
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

@Controller
@RequestMapping("/cte")
@RequiredArgsConstructor
@Slf4j
public class CteController {

    private final CteService cteService;
    private final com.sistema.nforaprazo.service.PdfExtractionService pdfExtractionService;

    @ResponseBody
    @PostMapping("/extrair-dados")
    public org.springframework.http.ResponseEntity<com.sistema.nforaprazo.dto.CteExtractionResultDto> extrairDadosPdf(
            @RequestParam("arquivoCte") org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().build();
        }
        java.io.File tempFile = null;
        try {
            tempFile = java.io.File.createTempFile("cte_extract_", ".pdf");
            file.transferTo(tempFile);
            com.sistema.nforaprazo.dto.CteExtractionResultDto dto = pdfExtractionService.extrairDadosCte(tempFile);
            return org.springframework.http.ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Erro ao extrair dados via AJAX: {}", e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).build();
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @GetMapping("/upload")
    public String exibirFormUpload(Model model) {
        model.addAttribute("cteUploadRequest", new CteUploadRequest());
        return "cte/upload";
    }

    @PostMapping("/upload")
    public String processarUpload(@Valid @ModelAttribute CteUploadRequest request,
                                  BindingResult bindingResult,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String erroMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("erro", "Erro de validação: " + erroMsg);
            return "redirect:/cte/upload";
        }

        try {
            Cte cteSalvo = cteService.processarUploadCte(request, authentication.getName());
            
            String msg = "CT-e importado com sucesso!";
            if (cteSalvo.getChaveAcesso() != null) {
                msg += " Chave: " + cteSalvo.getChaveAcesso();
            } else {
                msg += " Aviso: Chave não extraída automaticamente.";
            }
            if (cteSalvo.getStatus() == com.sistema.nforaprazo.model.enums.StatusCte.AGUARDANDO_DESEMBARACO) {
                msg += " Alerta enviado para equipe DESCARGA.";
            }
            redirectAttributes.addFlashAttribute("sucesso", msg);
            return "redirect:/cte/lista";
        } catch (Exception e) {
            log.error("Erro no upload", e);
            redirectAttributes.addFlashAttribute("erro", "Falha ao processar arquivo: " + e.getMessage());
            return "redirect:/cte/upload";
        }
    }
    
    @GetMapping("/lista")
    public String listarCtes(@RequestParam(defaultValue = "0") int page, Model model) {
        // Ordena pelos mais recentes
        Page<Cte> ctes = cteService.listarCtesPaginado(PageRequest.of(page, 15, Sort.by(Sort.Direction.DESC, "dataUpload")));
        model.addAttribute("ctes", ctes);
        return "cte/lista";
    }
}
