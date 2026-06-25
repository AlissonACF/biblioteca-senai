package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.CheckinRelatorioDTO;
import com.example.demo.dto.CheckoutRelatorioDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRepository;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private CheckinRepository checkinRepository;

    @Mock
    private CheckoutRepository checkoutRepository;

    @Test
    void deveListarCheckinsComDadosRelacionados() {
        RelatorioService service = new RelatorioService(checkinRepository, checkoutRepository);
        Checkin checkin = new Checkin();
        checkin.setId(1L);
        checkin.setCreatedAt(LocalDateTime.of(2026, 5, 27, 8, 30));
        checkin.setGuardaVidasEmail(" ");
        checkin.setPosto(posto(2L, "Posto Norte"));
        checkin.setFoto(arquivo("entrada.jpg"));

        when(checkinRepository.findAllAtivosOrderByCreatedAtDesc()).thenReturn(List.of(checkin));

        List<CheckinRelatorioDTO> resposta = service.listarCheckins();

        assertEquals(1, resposta.size());
        assertEquals(1L, resposta.get(0).getId());
        assertEquals(2L, resposta.get(0).getPostoId());
        assertEquals("Posto Norte", resposta.get(0).getPostoNome());
        assertEquals("Nao informado", resposta.get(0).getGuardaVidasEmail());
        assertEquals("entrada.jpg", resposta.get(0).getFotoNome());
    }

    @Test
    void deveListarCheckoutsMesmoSemPostoOuFoto() {
        RelatorioService service = new RelatorioService(checkinRepository, checkoutRepository);
        Checkout checkout = new Checkout();
        checkout.setId(3L);
        checkout.setCreatedAt(LocalDateTime.of(2026, 5, 27, 18, 0));
        checkout.setGuardaVidasEmail("guarda@vidas.com");
        checkout.setPrevencoes(40);
        checkout.setLesoes(1);

        when(checkoutRepository.findAllAtivosOrderByCreatedAtDesc()).thenReturn(List.of(checkout));

        List<CheckoutRelatorioDTO> resposta = service.listarCheckouts();

        assertEquals(3L, resposta.get(0).getId());
        assertEquals("guarda@vidas.com", resposta.get(0).getGuardaVidasEmail());
        assertEquals(40, resposta.get(0).getPrevencoes());
        assertEquals(1, resposta.get(0).getLesoes());
        assertNull(resposta.get(0).getPostoNome());
        assertNull(resposta.get(0).getFotoNome());
    }

    private Posto posto(Long id, String nome) {
        Posto posto = new Posto();
        posto.setId(id);
        posto.setNome(nome);
        return posto;
    }

    private Arquivo arquivo(String nome) {
        Arquivo arquivo = new Arquivo();
        arquivo.setNome(nome);
        return arquivo;
    }
}
