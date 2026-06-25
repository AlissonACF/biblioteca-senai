package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.config.JwtUtil;
import com.example.demo.dto.AuthDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwt;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAdmin;
    private String tokenPadrao;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();

        // Limpar usuários antes de cada teste
        usuarioRepository.deleteAll();

        // Criar usuário admin para testes
        Usuario admin = new Usuario();
        admin.setEmail("admin@teste.com");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setNivelAcesso(NivelAcesso.ADMIN);
        usuarioRepository.save(admin);

        // Criar usuário comum para testes
        Usuario user = new Usuario();
        user.setEmail("user@teste.com");
        user.setSenha(passwordEncoder.encode("user123"));
        user.setNivelAcesso(NivelAcesso.PADRAO);
        usuarioRepository.save(user);

        // Criar tokens para outros testes (se necessário)
        this.tokenAdmin = jwt.generateToken("admin@teste.com", NivelAcesso.ADMIN.toString());
        this.tokenPadrao = jwt.generateToken("user@teste.com", NivelAcesso.PADRAO.toString());
    }

    @Test
    @DisplayName("Deve fazer login com credenciais válidas e retornar token")
    void loginComCredenciaisValidas() throws Exception {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("admin@teste.com");
        authDTO.setSenha("admin123");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tipo").value("ADMIN"));
    }

    @Test
    @DisplayName("Deve fazer login com usuário comum e retornar token com tipo USER")
    void loginComUsuarioComum() throws Exception {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("user@teste.com");
        authDTO.setSenha("user123");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tipo").value("PADRAO"));
    }

    @Test
    @DisplayName("Deve retornar erro 401 quando email não existe")
    void loginComEmailInexistente() throws Exception {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("naoexiste@teste.com");
        authDTO.setSenha("qualquer123");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciais Inválidas!"));
    }

    @Test
    @DisplayName("Deve retornar erro 401 quando senha está incorreta")
    void loginComSenhaIncorreta() throws Exception {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("admin@teste.com");
        authDTO.setSenha("senhaerrada");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciais Inválidas!"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando email é nulo")
    void loginComEmailNulo() throws Exception {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail(null);
        authDTO.setSenha("admin123");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando email está vazio")
    void loginComEmailVazio() throws Exception {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("");
        authDTO.setSenha("admin123");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando senha é nula")
    void loginComSenhaNula() throws Exception {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("admin@teste.com");
        authDTO.setSenha(null);

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando senha está vazia")
    void loginComSenhaVazia() throws Exception {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("admin@teste.com");
        authDTO.setSenha("");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando email tem formato inválido")
    void loginComEmailFormatoInvalido() throws Exception {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("emailinvalido");
        authDTO.setSenha("admin123");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve acessar endpoint ping publico sem autenticação")
    void pingEndpointPublico() throws Exception {
        mockMvc.perform(get("/auth/ping"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve acessar endpoint ping com token válido (redundante, mas testa acesso autenticado)")
    void pingComTokenValido() throws Exception {
        mockMvc.perform(get("/auth/ping")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve fazer login com email em maiúsculo e minúsculo misturados")
    void loginComEmailCaseInsensitive() throws Exception {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setEmail("Admin@Teste.com"); // Case diferente
        authDTO.setSenha("admin123");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}