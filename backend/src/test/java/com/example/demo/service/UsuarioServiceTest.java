package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.RecuperacaoSolicitacaoDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private EmailService emailService;

    @Test
    void deveGerarCodigoSalvarUsuarioEEnviarEmail() throws Exception {
        UsuarioService service = service();
        Usuario usuario = new Usuario();
        usuario.setEmail("user@teste.com");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        when(repository.findByEmail("user@teste.com")).thenReturn(Optional.of(usuario));

        service.solicitarCodigo(new RecuperacaoSolicitacaoDTO("user@teste.com"));

        verify(repository).save(captor.capture());
        assertNotNull(captor.getValue().getCodigoRecuperacao());
        assertEquals(8, captor.getValue().getCodigoRecuperacao().length());
        assertNotNull(captor.getValue().getCodigoRecuperacaoExpiracao());
        verify(emailService).enviarEmail(eq("user@teste.com"), anyString(), anyString());
    }

    @Test
    void deveFalharQuandoEmailNaoExiste() {
        UsuarioService service = service();
        when(repository.findByEmail("nao@existe.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.solicitarCodigo(new RecuperacaoSolicitacaoDTO("nao@existe.com")));
    }

    @Test
    void deveRetornarErroInternoQuandoEmailFalha() throws Exception {
        UsuarioService service = service();
        Usuario usuario = new Usuario();
        usuario.setEmail("user@teste.com");

        when(repository.findByEmail("user@teste.com")).thenReturn(Optional.of(usuario));
        doThrow(new RuntimeException("smtp fora")).when(emailService)
                .enviarEmail(eq("user@teste.com"), anyString(),
                        org.mockito.ArgumentMatchers.anyString());

        assertThrows(ResponseStatusException.class,
                () -> service.solicitarCodigo(new RecuperacaoSolicitacaoDTO("user@teste.com")));
    }

    private UsuarioService service() {
        UsuarioService service = new UsuarioService(repository);
        ReflectionTestUtils.setField(service, "emailService", emailService);
        return service;
    }
}
