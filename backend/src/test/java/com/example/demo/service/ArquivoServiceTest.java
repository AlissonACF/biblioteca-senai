package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Arquivo;
import com.example.demo.repository.ArquivoRepository;

@ExtendWith(MockitoExtension.class)
class ArquivoServiceTest {

    @Mock
    private ArquivoRepository arquivoRepository;

    @Mock
    private MultipartFile arquivoComErro;

    @TempDir
    private Path tempDir;

    @Test
    void deveSalvarArquivoNoDiscoENoRepositorio() {
        ArquivoService service = service(tempDir.resolve("uploads"));
        MockMultipartFile file = new MockMultipartFile("foto", "mar.jpg", "image/jpeg", "conteudo".getBytes());

        when(arquivoRepository.save(any(Arquivo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Arquivo resposta = service.upload(file);

        assertEquals("mar.jpg", resposta.getNome());
        assertEquals("image/jpeg", resposta.getTipo());
        assertEquals(file.getSize(), resposta.getTamanho());
        assertTrue(Files.exists(Path.of(resposta.getCaminho())));
    }

    @Test
    void deveConverterIOExceptionEmRuntimeException() throws IOException {
        ArquivoService service = service(tempDir);
        when(arquivoComErro.getInputStream()).thenThrow(new IOException("falha"));

        assertThrows(RuntimeException.class, () -> service.upload(arquivoComErro));
    }

    private ArquivoService service(Path path) {
        ArquivoService service = new ArquivoService();
        ReflectionTestUtils.setField(service, "path", path.toString());
        ReflectionTestUtils.setField(service, "arquivoRepository", arquivoRepository);
        return service;
    }
}
