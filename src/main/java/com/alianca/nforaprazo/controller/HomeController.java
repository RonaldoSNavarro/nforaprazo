package com.alianca.nforaprazo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        String email = authentication.getName();
        String perfil = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("DESCONHECIDO");

        model.addAttribute("email", email);
        model.addAttribute("perfil", perfil);
        return "home";
    }
}
