package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.PostoDTO;
import com.example.demo.entity.Posto;
import com.example.demo.enums.PostoStatus;
import com.example.demo.repository.PostoRepository;

@ExtendWith(MockitoExtension.class)
class BaseAndPostoServiceTest {

    @Mock
    private PostoRepository repository;

    @Test
    void deveCriarAtualizarLerListarEExcluirPosto() {
        PostoService service = new PostoService(repository);
        Posto salvo = posto(7L, "Central", "Descricao", PostoStatus.MANUTENCAO);

        when(repository.save(any(Posto.class))).thenReturn(salvo);
        when(repository.findById(7L)).thenReturn(Optional.of(salvo));
        when(repository.findAll()).thenReturn(List.of(salvo));

        PostoDTO dto = new PostoDTO(null, "Central", "Descricao", PostoStatus.MANUTENCAO);

        PostoDTO criado = service.create(dto);
        PostoDTO atualizado = service.update(7L, dto);
        PostoDTO lido = service.read(7L);
        List<PostoDTO> todos = service.read();
        service.delete(7L);
        service.softDelete(7L);

        assertEquals(7L, criado.getId());
        assertEquals(7L, atualizado.getId());
        assertEquals("Central", lido.getNome());
        assertEquals(1, todos.size());
        verify(repository).deleteById(7L);
        verify(repository).softDeleteById(7L);
    }

    @Test
    void deveDefinirStatusDisponivelQuandoDtoNaoInformaStatus() {
        PostoService service = new PostoService(repository);
        ArgumentCaptor<Posto> captor = ArgumentCaptor.forClass(Posto.class);

        when(repository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new PostoDTO(null, "Posto", "Livre", null));

        assertEquals(PostoStatus.DISPONIVEL, captor.getValue().getStatus());
    }

    @Test
    void devePropagarErroQuandoIdNaoExiste() {
        PostoService service = new PostoService(repository);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.read(99L));
    }

    private Posto posto(Long id, String nome, String descricao, PostoStatus status) {
        Posto posto = new Posto();
        posto.setId(id);
        posto.setNome(nome);
        posto.setDescricao(descricao);
        posto.setStatus(status);
        return posto;
    }
}
