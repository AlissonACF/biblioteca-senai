package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dto.CheckinDTO;
import com.example.demo.dto.CheckinResponseDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Posto;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.PostoRepository;

@ExtendWith(MockitoExtension.class)
class CheckServiceTest {

    @Mock
    private PostoRepository postoRepository;

    @Mock
    private ArquivoService arquivoService;

    @Mock
    private CheckinRepository checkinRepository;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveFazerCheckinComFotoEUsuarioAutenticado() {
        CheckService service = service();
        Posto posto = new Posto();
        posto.setId(1L);
        posto.setNome("Posto 1");
        Arquivo arquivo = new Arquivo();
        MockMultipartFile foto = new MockMultipartFile("foto", "a.jpg", "image/jpeg", "abc".getBytes());
        CheckinDTO dto = new CheckinDTO();
        dto.setPostoId(1L);
        dto.setFoto(foto);
        LocalDateTime horario = LocalDateTime.of(2026, 5, 27, 9, 0);
        ArgumentCaptor<Checkin> captor = ArgumentCaptor.forClass(Checkin.class);

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("guarda@vidas.com", null));
        when(postoRepository.findById(1L)).thenReturn(Optional.of(posto));
        when(arquivoService.upload(foto)).thenReturn(arquivo);
        when(checkinRepository.save(captor.capture())).thenAnswer(invocation -> {
            Checkin checkin = invocation.getArgument(0);
            checkin.setCreatedAt(horario);
            return checkin;
        });

        CheckinResponseDTO resposta = service.checkin(dto);

        assertEquals("Posto 1", resposta.getPosto());
        assertEquals(horario, resposta.getHorario());
        assertEquals("guarda@vidas.com", captor.getValue().getGuardaVidasEmail());
        assertEquals(arquivo, captor.getValue().getFoto());
    }

    @Test
    void deveFalharQuandoPostoNaoExiste() {
        CheckService service = service();
        CheckinDTO dto = new CheckinDTO();
        dto.setPostoId(99L);
        when(postoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.checkin(dto));
    }

    private CheckService service() {
        CheckService service = new CheckService();
        ReflectionTestUtils.setField(service, "postoRepository", postoRepository);
        ReflectionTestUtils.setField(service, "arquivoService", arquivoService);
        ReflectionTestUtils.setField(service, "checkinRepository", checkinRepository);
        return service;
    }
}
