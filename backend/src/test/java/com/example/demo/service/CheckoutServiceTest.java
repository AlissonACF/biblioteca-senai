package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.dto.CheckoutDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.repository.CheckoutRepository;
import com.example.demo.repository.PostoRepository;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CheckoutRepository checkoutRepository;

    @Mock
    private PostoRepository postoRepository;

    @Mock
    private ArquivoService arquivoService;

    @BeforeEach
    void prepararContexto() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveRegistrarCheckoutComFotoEUsuarioAutenticado() {
        CheckoutService service = new CheckoutService(checkoutRepository, postoRepository, arquivoService);
        Posto posto = new Posto();
        posto.setId(4L);
        MockMultipartFile foto = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", "abc".getBytes());
        Arquivo arquivo = new Arquivo();
        arquivo.setNome("foto.jpg");
        CheckoutDTO dto = new CheckoutDTO(null, 4L, 12, 2, foto);
        ArgumentCaptor<Checkout> captor = ArgumentCaptor.forClass(Checkout.class);

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("salva@vidas.com", null));
        when(postoRepository.findById(4L)).thenReturn(Optional.of(posto));
        when(arquivoService.upload(foto)).thenReturn(arquivo);
        when(checkoutRepository.save(captor.capture())).thenAnswer(invocation -> {
            Checkout checkout = invocation.getArgument(0);
            checkout.setId(10L);
            return checkout;
        });

        CheckoutDTO resposta = service.registrar(dto);

        assertEquals(10L, resposta.getId());
        assertEquals(4L, resposta.getPostoId());
        assertEquals(12, resposta.getPrevencoes());
        assertEquals(2, resposta.getLesoes());
        assertEquals("salva@vidas.com", captor.getValue().getGuardaVidasEmail());
        assertEquals(arquivo, captor.getValue().getFoto());
    }

    @Test
    void deveRegistrarSemFotoESemUsuarioAutenticado() {
        CheckoutService service = new CheckoutService(checkoutRepository, postoRepository, arquivoService);
        Posto posto = new Posto();
        posto.setId(5L);
        CheckoutDTO dto = new CheckoutDTO(null, 5L, 1, 0, null);
        ArgumentCaptor<Checkout> captor = ArgumentCaptor.forClass(Checkout.class);

        when(postoRepository.findById(5L)).thenReturn(Optional.of(posto));
        when(checkoutRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutDTO resposta = service.create(dto);

        assertEquals(5L, resposta.getPostoId());
        assertNull(captor.getValue().getGuardaVidasEmail());
        verify(arquivoService, never()).upload(any());
    }

    @Test
    void deveFalharQuandoPostoNaoExiste() {
        CheckoutService service = new CheckoutService(checkoutRepository, postoRepository, arquivoService);
        when(postoRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.registrar(new CheckoutDTO(null, 55L, 1, 1, null)));
    }
}
