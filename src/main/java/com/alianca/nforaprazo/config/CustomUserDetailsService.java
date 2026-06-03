package com.alianca.nforaprazo.config;

import com.alianca.nforaprazo.model.Usuario;
import com.alianca.nforaprazo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        if (!usuario.getAtivo()) {
            throw new UsernameNotFoundException("Usuario desativado");
        }

        return new User(
                usuario.getEmail(),
                usuario.getSenha(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name())
                )
        );
    }
}
