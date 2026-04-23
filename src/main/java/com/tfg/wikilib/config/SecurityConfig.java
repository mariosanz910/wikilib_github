package com.tfg.wikilib.config;

import com.tfg.wikilib.model.Usuario;
import com.tfg.wikilib.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final UsuarioService usuarioService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UsuarioService usuarioService,
                          UserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);

        return authManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas (sin autenticación)
                        .requestMatchers("/", "/login", "/registro", "/catalogo/**", "/publicacion/**", "/css/**", "/js/**", "/error").permitAll()

                        // Rutas para ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Rutas para REDACTOR
                        .requestMatchers("/redactor/**").hasRole("REDACTOR")

                        // Rutas para USUARIO autenticado (cualquier rol)
                        .requestMatchers("/perfil/**", "/favoritos/**").authenticated()

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureUrl("/login?error")
                        .permitAll())

                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/logout"));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            String username = authentication.getName();

            try {
                Usuario usuario = usuarioService.buscarPorNombreUsuario(username);
                // Aquí podrías actualizar fecha de último login si quisieras
                // usuarioService.actualizarUsuario(usuario);
            } catch (Exception e) {
                System.err.println("Error al obtener usuario: " + e.getMessage());
            }

            // Redirigir según rol
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            boolean isRedactor = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_REDACTOR"));

            boolean isUsuario = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USUARIO"));

            if (isAdmin) {
                response.sendRedirect("/admin/dashboard"); // Panel de administración
            } else if (isRedactor) {
                response.sendRedirect("/redactor/panel"); // Panel del redactor
            } else if (isUsuario) {
                response.sendRedirect("/catalogo"); // Catálogo de colecciones
            } else {
                response.sendRedirect("/login");
            }
        };
    }
}