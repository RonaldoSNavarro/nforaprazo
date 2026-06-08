package com.sistema.nforaprazo.repository;

import com.sistema.nforaprazo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    java.util.List<Usuario> findByPerfilAndAtivoTrue(com.sistema.nforaprazo.model.enums.PerfilUsuario perfil);
}
