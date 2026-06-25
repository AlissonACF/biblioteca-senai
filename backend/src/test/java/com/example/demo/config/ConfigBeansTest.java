package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import com.example.demo.entity.Posto;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ConfigBeansTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PostoRepository postoRepository;

    @Test
    void deveCriarBeansDeCorsSwaggerESecurity() {
        CorsConfigurationSource corsConfigurationSource = new CorsConfig().corsConfigurationSource();
        assertNotNull(corsConfigurationSource);
        assertNotNull(new SwaggerConfig().customOpenAPI());
        assertNotNull(new SecurityConfig().passwordEncoder());
        assertDoesNotThrow(() -> new SwaggerStartupListener().onStart());
    }

    @Test
    void deveInicializarUsuariosQuandoBancoVazio() throws Exception {
        DataInitializer initializer = new DataInitializer(passwordEncoder);
        CommandLineRunner runner = initializer.initDatabase(usuarioRepository);
        when(passwordEncoder.encode("admin123")).thenReturn("admin-hash");
        when(passwordEncoder.encode("aluno123")).thenReturn("aluno-hash");

        runner.run();

        verify(usuarioRepository, org.mockito.Mockito.times(2)).save(org.mockito.Mockito.any(Usuario.class));
    }

    @Test
    void deveAtualizarUsuariosPadraoQuandoJaExistem() throws Exception {
        DataInitializer initializer = new DataInitializer(passwordEncoder);
        CommandLineRunner runner = initializer.initDatabase(usuarioRepository);
        Usuario admin = new Usuario();
        admin.setEmail("Admin@SENAI.br");
        admin.setSenha("senha-antiga");
        admin.setAtivo(false);
        Usuario aluno = new Usuario();
        aluno.setEmail("Aluno@SENAI.br");
        aluno.setSenha("senha-antiga");
        aluno.setAtivo(false);

        when(usuarioRepository.findAnyByEmail("admin@senai.br")).thenReturn(java.util.Optional.of(admin));
        when(usuarioRepository.findAnyByEmail("aluno@senai.br")).thenReturn(java.util.Optional.of(aluno));
        when(passwordEncoder.encode("admin123")).thenReturn("admin-hash");
        when(passwordEncoder.encode("aluno123")).thenReturn("aluno-hash");

        runner.run();

        verify(usuarioRepository, org.mockito.Mockito.times(2)).save(org.mockito.Mockito.any(Usuario.class));
        assertEquals("admin@senai.br", admin.getEmail());
        assertEquals("admin-hash", admin.getSenha());
        assertEquals(true, admin.isAtivo());
        assertEquals("aluno@senai.br", aluno.getEmail());
        assertEquals("aluno-hash", aluno.getSenha());
        assertEquals(true, aluno.isAtivo());
    }

    @Test
    void deveCriarVinteEUmPostosQuandoBancoVazio() throws Exception {
        DataInitializer initializer = new DataInitializer(passwordEncoder);
        CommandLineRunner runner = initializer.initPostos(postoRepository);
        when(postoRepository.findTodosOrderByIdAsc()).thenReturn(List.of());

        runner.run();

        verify(postoRepository, org.mockito.Mockito.times(21)).save(org.mockito.Mockito.any(Posto.class));
    }

    @Test
    void deveNormalizarPostosQuandoJaExistem() throws Exception {
        DataInitializer initializer = new DataInitializer(passwordEncoder);
        CommandLineRunner runner = initializer.initPostos(postoRepository);
        Posto posto = new Posto();
        posto.setId(7L);

        when(postoRepository.findTodosOrderByIdAsc()).thenReturn(List.of(posto));

        runner.run();

        verify(postoRepository, org.mockito.Mockito.times(20)).save(org.mockito.Mockito.any(Posto.class));
        verify(postoRepository).saveAll(org.mockito.Mockito.anyList());
        assertEquals("Posto 7", posto.getNome());
    }
}
