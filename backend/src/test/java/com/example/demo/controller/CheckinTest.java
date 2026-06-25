package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.config.JwtUtil;
import com.example.demo.entity.Posto;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.PostoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
public class CheckinTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwt;

    private String token;

    @Autowired
    private PostoRepository pr;

    
    private Posto posto;

    @BeforeEach
    public void setup(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();

        this.token = jwt.generateToken("tantofazcomotantofez@admin.com", 
        NivelAcesso.ADMIN.toString());

        this.posto = new Posto();

        posto.setNome("Exemplos");
        posto.setDescricao("Exemplo");

        posto = pr.save(posto);
    }

    @Test
    @DisplayName("Fazer checkin")
    void checkin() throws Exception {
        MockMultipartFile fotoMock = new MockMultipartFile(
        "foto", 
        "bombeiro.jpg", 
        MediaType.IMAGE_JPEG_VALUE, "conteudo".getBytes());

        mockMvc.perform(multipart("/check/in").file(fotoMock).param("postoId", posto.getId().toString())
        .header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }
}
