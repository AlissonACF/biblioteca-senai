package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.config.JwtUtil;
import com.example.demo.dto.PostoDTO;
import com.example.demo.entity.Posto;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRepository;
import com.example.demo.repository.PostoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;


@SpringBootTest
@ActiveProfiles("test")
public class PostoControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwt;

    private String token;

    @Autowired
    private PostoRepository pr;

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private CheckoutRepository checkoutRepository;

    @BeforeEach
    public void setup(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();

        this.token = jwt.generateToken("tantofazcomotantofez@admin.com", NivelAcesso.ADMIN.toString());

        checkinRepository.deleteAll();
        checkoutRepository.deleteAll();
        pr.deleteAll();
    }

    @Test
    @DisplayName("Deve buscar posto pelo ID")
    void buscarPorId() throws Exception {
        Posto posto = new Posto();
        posto.setNome("Posto para buscar por ID");
        posto.setDescricao("Posto Buscável");

        posto = pr.save(posto);

        mockMvc.perform(get("/postos/" + posto.getId())
        .header("Authorization", "Bearer " + token)).andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("Posto para buscar por ID"));
    }

    @Test
    @DisplayName("Deve criar um posto com sucesso")
    void criarPosto() throws Exception {
        PostoDTO postoDTO = new PostoDTO();

        postoDTO.setNome("Posto 1");
        postoDTO.setDescricao("Posto 1 em manutenção");

        String json = objectMapper.writeValueAsString(postoDTO);

        mockMvc.perform(post("/postos").contentType(MediaType.APPLICATION_JSON).content(json)
        .header("Authorization", "Bearer " + token)).andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("Posto 1")).andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve listar todos os postos")
    void listarPostos() throws Exception {
        mockMvc.perform(get("/postos").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve tentar criar um posto vazio e retornar um erro")
    void criarPostoBad() throws Exception {
        PostoDTO postoDTO = new PostoDTO();

        postoDTO.setNome("");
        postoDTO.setDescricao("");

        String json = objectMapper.writeValueAsString(postoDTO);

        mockMvc.perform(post("/postos").contentType(MediaType.APPLICATION_JSON).content(json)
        .header("Authorization", "Bearer " + token)).andExpect(status().isBadRequest());
    }

}
