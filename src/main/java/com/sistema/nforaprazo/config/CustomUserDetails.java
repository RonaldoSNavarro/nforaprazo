package com.sistema.nforaprazo.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final boolean alterarSenha;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, boolean alterarSenha) {
        super(username, password, authorities);
        this.alterarSenha = alterarSenha;
    }
}
