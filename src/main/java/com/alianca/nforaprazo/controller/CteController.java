package com.alianca.nforaprazo.controller;

import com.alianca.nforaprazo.model.Cte;
import com.alianca.nforaprazo.service.CteService;
import com.alianca.nforaprazo.repository.CteRepository;
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

@Controller
@RequestMapping("/cte")
@RequiredArgsConstructor
@Slf4j
public class CteController {

    private final CteService cteService;
    private final CteRepository cteRepository;

    @GetMapping("/upload")
    public String exibirFormUpload() {
        return "cte/upload";
    }

    @PostMapping("/upload")
    public String processarUpload(@RequestParam("arquivoCte") MultipartFile file,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            Cte cteSalvo = cteService.processarUploadCte(file, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "CT-e importado com sucesso! " + 
                (cteSalvo.getChaveAcesso() != null ? "Chave: " + cteSalvo.getChaveAcesso() : "Aviso: Chave nao extraída automaticamente."));
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
        Page<Cte> ctes = cteRepository.findAll(PageRequest.of(page, 15, Sort.by(Sort.Direction.DESC, "dataUpload")));
        model.addAttribute("ctes", ctes);
        return "cte/lista";
    }
}
