package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.CorsFilter;

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
        CorsFilter corsFilter = new CorsConfig().corsFilter();
        assertNotNull(corsFilter);
        assertNotNull(new SwaggerConfig().customOpenAPI());
        assertNotNull(new SecurityConfig().passwordEncoder());
        assertDoesNotThrow(() -> new SwaggerStartupListener().onStart());
    }

    @Test
    void deveInicializarUsuariosQuandoBancoVazio() throws Exception {
        DataInitializer initializer = new DataInitializer(passwordEncoder);
        CommandLineRunner runner = initializer.initDatabase(usuarioRepository);
        when(usuarioRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("123456789")).thenReturn("admin-hash");
        when(passwordEncoder.encode("user123")).thenReturn("user-hash");

        runner.run();

        verify(usuarioRepository, org.mockito.Mockito.times(2)).save(org.mockito.Mockito.any(Usuario.class));
    }

    @Test
    void naoDeveInicializarUsuariosQuandoJaExistem() throws Exception {
        DataInitializer initializer = new DataInitializer(passwordEncoder);
        CommandLineRunner runner = initializer.initDatabase(usuarioRepository);
        when(usuarioRepository.count()).thenReturn(1L);

        runner.run();

        verify(usuarioRepository, never()).save(org.mockito.Mockito.any(Usuario.class));
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
