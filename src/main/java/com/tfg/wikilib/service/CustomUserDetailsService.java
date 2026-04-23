package com.tfg.wikilib.service;

import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        /*System.out.println("✅ Usuario encontrado en BD:");
        System.out.println("   - ID: " + usuario.getId());
        System.out.println("   - Nombre: " + usuario.getNombreUsuario());
        System.out.println("   - Email: " + usuario.getEmail());
        System.out.println("   - Rol: " + usuario.getRol());
        System.out.println("   - Estado: " + usuario.getEstado());
        System.out.println("   - Password (primeros 30 chars): " + usuario.getPassword().substring(0, Math.min(30, usuario.getPassword().length())) + "...");
*/
        // Verificar que el usuario esté activo
        if (usuario.getEstado() == Usuario.Estado.INACTIVO) {
            throw new RuntimeException("Cuenta inactiva");
        }

        return User.builder()
                .username(usuario.getNombreUsuario())
                .password(usuario.getPassword())
                .authorities(getAuthorities(usuario))
                .accountExpired(false)
                .accountLocked(usuario.getEstado() == Usuario.Estado.INACTIVO)
                .credentialsExpired(false)
                .disabled(usuario.getEstado() == Usuario.Estado.INACTIVO)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }


}