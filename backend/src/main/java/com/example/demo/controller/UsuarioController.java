package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Admin;
import com.example.demo.dto.CadastroUsuarioDTO;
import com.example.demo.dto.UsuarioDTO;
import com.example.demo.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController extends BaseController<UsuarioDTO> {

    @Autowired
    private UsuarioService usuarioService;

    public UsuarioController(UsuarioService service) {
        super(service);
    }

    /**
     * Admin cria um usuário comum pelo CPF.
     * Senha gerada automaticamente = primeiros 4 dígitos do CPF.
     */
    @Admin
    @PostMapping("/cpf")
    public ResponseEntity<CadastroUsuarioDTO> criarPorCpf(@RequestBody @Valid CadastroUsuarioDTO dto) {
        CadastroUsuarioDTO criado = usuarioService.criarPorCpf(dto);
        return ResponseEntity.ok(criado);
    }

    /**
     * Admin lista todos os usuários comuns cadastrados por CPF.
     */
    @Admin
    @GetMapping("/cpf")
    public ResponseEntity<List<CadastroUsuarioDTO>> listarUsuariosComuns() {
        return ResponseEntity.ok(usuarioService.listarUsuariosComuns());
    }

    /**
     * Admin remove um usuário comum (soft delete).
     */
    @Admin
    @DeleteMapping("/cpf/{id}")
    public ResponseEntity<Void> removerUsuario(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
