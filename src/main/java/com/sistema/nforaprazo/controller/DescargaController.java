package com.sistema.nforaprazo.controller;

import com.sistema.nforaprazo.dto.*;
import com.sistema.nforaprazo.model.Cte;
import com.sistema.nforaprazo.model.enums.StatusCte;
import com.sistema.nforaprazo.repository.CteRepository;
import com.sistema.nforaprazo.service.DescargaService;
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
@RequestMapping("/descarga")
@RequiredArgsConstructor
@Slf4j
public class DescargaController {

    private final CteRepository cteRepository;
    private final DescargaService descargaService;

    @GetMapping("/pendentes")
    public String listarPendentes(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Cte> pendentes = cteRepository.findByStatusIn(
                List.of(
                        StatusCte.PENDENTE,
                        StatusCte.AGUARDANDO_DESEMBARACO,
                        StatusCte.DESEMBARACADO,
                        StatusCte.AUTO_RECEBIDO,
                        StatusCte.EM_INVESTIGACAO,
                        StatusCte.AGUARDANDO_PAGAMENTO
                ), 
                PageRequest.of(page, 15, Sort.by(Sort.Direction.ASC, "dataUpload")));
        
        model.addAttribute("ctes", pendentes);
        return "descarga/pendentes";
    }

    @GetMapping("/pagamentos")
    public String listarPagamentos(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Cte> pagamentos = cteRepository.findByStatusIn(
                List.of(StatusCte.AGUARDANDO_PAGAMENTO),
                PageRequest.of(page, 15, Sort.by(Sort.Direction.ASC, "dataUpload")));

        model.addAttribute("ctes", pagamentos);
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
    public String registrarAutoInfracao(@Valid @ModelAttribute AutoInfracaoRequest request,
                                        BindingResult bindingResult,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String erroMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("erro", "Erro de validação: " + erroMsg);
            return "redirect:/descarga/pendentes";
        }
        try {
            descargaService.registrarAutoInfracao(request, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Auto de Infração registrado com sucesso!");
        } catch (Exception e) {
            log.error("Erro", e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/descarga/pendentes";
    }

    @PostMapping("/dar")
    public String registrarDar(@Valid @ModelAttribute DarRequest request,
                               BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String erroMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("erro", "Erro de validação: " + erroMsg);
            return "redirect:/descarga/pendentes";
        }
        try {
            descargaService.registrarDar(request, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "DAR registrado com sucesso!");
        } catch (Exception e) {
            log.error("Erro", e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/descarga/pendentes";
    }

    @PostMapping("/comprovante")
    public String registrarComprovante(@Valid @ModelAttribute ComprovanteRequest request,
                                       BindingResult bindingResult,
                                       Authentication authentication, 
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String erroMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("erro", "Erro de validação: " + erroMsg);
            return "redirect:/descarga/pendentes";
        }
        try {
            descargaService.registrarComprovante(request, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Comprovante registrado e pagamento finalizado!");
        } catch (Exception e) {
            log.error("Erro", e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/descarga/pendentes";
    }

    @PostMapping("/encerrar-sem-auto")
    public String encerrarSemAuto(@Valid @ModelAttribute EncSemAutoRequest request,
                                  BindingResult bindingResult,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String erroMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("erro", "Erro de validação: " + erroMsg);
            return "redirect:/descarga/pendentes";
        }
        try {
            descargaService.encerrarSemAuto(request, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Processo encerrado sem auto de infração com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao encerrar sem auto para CT-e {}", request.getCteId(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro: " + e.getMessage());
        }
        return "redirect:/descarga/pendentes";
    }
}
