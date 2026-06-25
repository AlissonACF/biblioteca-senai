package com.example.demo.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Public;
import com.example.demo.config.JwtUtil;
import com.example.demo.dto.AuthDTO;
import com.example.demo.dto.RecuperacaoSolicitacaoDTO;
import com.example.demo.dto.RecuperarSenhaDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    @Public
    public ResponseEntity<?> login(@RequestBody @Valid AuthDTO dto) {
        String identificador = dto.getEmail() == null ? "" : dto.getEmail().trim();
        String senha = dto.getSenha();

        Optional<Usuario> usuarioOpt;

        if (identificador.matches("\\d{11}")) {
            usuarioOpt = usuarioRepository.findByCpf(identificador);
        } else if (!identificador.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return ResponseEntity.badRequest().body("Identificador invalido!");
        } else {
            usuarioOpt = usuarioRepository.findByEmail(identificador.toLowerCase());
        }

        if (usuarioOpt.isPresent() && passwordEncoder.matches(senha, usuarioOpt.get().getSenha())) {
            String nivelAcesso = usuarioOpt.get().getNivelAcesso().toString();
            String tokenSubject = usuarioOpt.get().getEmail();

            Usuario usuario = usuarioOpt.get();
            String token = jwtUtil.generateToken(tokenSubject, nivelAcesso);

            return ResponseEntity.ok(Map.of(
                "token", token,
                "tipo", nivelAcesso,
                "user", Map.of(
                    "id", "user-" + usuario.getId(),
                    "name", usuario.getNome(),
                    "email", usuario.getEmail(),
                    "password", "",
                    "role", usuario.getNivelAcesso() == com.example.demo.enums.NivelAcesso.ADMIN ? "admin" : "user"
                )
            ));
        }

        return ResponseEntity.status(401).body("Credenciais Inv\u00e1lidas!");
    }

    @GetMapping("/ping")
    @Public
    public void pong() {
    }

    @Public
    @PostMapping("/recuperar-senha/solicitar")
    public ResponseEntity<?> solicitarCodigo(@RequestBody @Valid RecuperacaoSolicitacaoDTO dto) {
        usuarioService.solicitarCodigo(dto);
        return ResponseEntity.ok(Map.of("message", "E-mail enviado com sucesso!"));
    }

    @Public
    @PostMapping("/recuperar-senha/alterar")
    public ResponseEntity<?> alterarSenha(@RequestBody @Valid RecuperarSenhaDTO dto) {
        usuarioService.trocarSenha(dto);
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso!"));
    }
}
