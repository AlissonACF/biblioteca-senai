package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import com.example.demo.repository.CheckoutRepository;
import com.example.demo.repository.PostoRepository;

@SpringBootTest
@ActiveProfiles("test")
public class CheckoutControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtUtil jwt;

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private PostoRepository postoRepository;

    private String tokenUser;
    private Posto posto;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.tokenUser = jwt.generateToken("guardavidas@teste.com", NivelAcesso.PADRAO.toString());

        checkoutRepository.deleteAll();

        posto = new Posto();
        posto.setNome("Posto Central");
        posto.setDescricao("Posto principal da praia central");
        posto = postoRepository.save(posto);
    }

    @Test
    @DisplayName("Deve criar um checkout com sucesso")
    void criarCheckout() throws Exception {
        MockMultipartFile fotoMock = new MockMultipartFile(
            "foto",
            "checkout.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "conteudo".getBytes()
        );

        mockMvc.perform(multipart("/checkout/registrar")
                .file(fotoMock)
                .param("postoId", posto.getId().toString())
                .param("prevencoes", "50")
                .param("lesoes", "3")
                .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.postoId").value(posto.getId()))
                .andExpect(jsonPath("$.prevencoes").value(50))
                .andExpect(jsonPath("$.lesoes").value(3));
    }

    @Test
    @DisplayName("Deve rejeitar checkout sem posto")
    void criarCheckoutSemPosto() throws Exception {
        mockMvc.perform(multipart("/checkout/registrar")
                .param("prevencoes", "10")
                .param("lesoes", "1")
                .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve rejeitar checkout com numero negativo")
    void criarCheckoutComNumeroNegativo() throws Exception {
        mockMvc.perform(multipart("/checkout/registrar")
                .param("postoId", posto.getId().toString())
                .param("prevencoes", "-1")
                .param("lesoes", "0")
                .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve listar checkouts")
    void listarCheckouts() throws Exception {
        mockMvc.perform(get("/checkout")
                .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isOk());
    }
}
