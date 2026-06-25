package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CadastroUsuarioDTO;
import com.example.demo.dto.RecuperacaoSolicitacaoDTO;
import com.example.demo.dto.RecuperarSenhaDTO;
import com.example.demo.dto.UsuarioDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService extends BaseService<Usuario, UsuarioDTO> {

    private UsuarioRepository repository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository) {
        super(repository);
        this.repository = repository;
    }

    /**
     * Cria um usuário comum a partir do CPF.
     * O email é cpf@cpf.com, a senha é os primeiros 4 dígitos do CPF.
     */
    @Transactional
    public CadastroUsuarioDTO criarPorCpf(CadastroUsuarioDTO dto) {
        String cpf = dto.getCpf().replaceAll("\\D", "");

        if (cpf.length() != 11) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF deve ter exatamente 11 dígitos.");
        }

        if (repository.findByCpf(cpf).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado.");
        }

        String email = cpf + "@cpf.com";
        if (repository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário com este CPF já existe.");
        }

        String senha = cpf.substring(0, 4);

        Usuario usuario = new Usuario();
        usuario.setNome("Usuario " + cpf.substring(0, 3));
        usuario.setCpf(cpf);
        usuario.setEmail(email);
        usuario.setNivelAcesso(NivelAcesso.PADRAO);
        usuario.setSenha(passwordEncoder.encode(senha));

        Usuario salvo = repository.save(usuario);

        CadastroUsuarioDTO resposta = new CadastroUsuarioDTO();
        resposta.setId(salvo.getId());
        resposta.setCpf(cpf);
        resposta.setSenhaGerada(senha);
        return resposta;
    }

    /**
     * Lista todos os usuários comuns (não admin) ativos.
     */
    public List<CadastroUsuarioDTO> listarUsuariosComuns() {
        return repository.findAll().stream()
            .filter(u -> u.isAtivo() && u.getNivelAcesso() == NivelAcesso.PADRAO && u.getCpf() != null)
            .map(u -> {
                CadastroUsuarioDTO dto = new CadastroUsuarioDTO();
                dto.setId(u.getId());
                dto.setCpf(u.getCpf());
                return dto;
            })
            .toList();
    }

    @Transactional
    public void solicitarCodigo(RecuperacaoSolicitacaoDTO dto) {

        String email = dto.getEmail();

        Usuario usuario = repository.findByEmail(email).orElseThrow();

        String codigo = String.valueOf(10000000 + new Random().nextInt(90000000));

        usuario.setCodigoRecuperacao(codigo);
        usuario.setCodigoRecuperacaoExpiracao(LocalDateTime.now().plusMinutes(20));

        repository.save(usuario);

        try {
            emailService.enviarEmail(email, "SOLICITAÇÃO DE RECUPERAÇÃO DE SENHA", "SEU CÓDIGO É: " + codigo);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "DEU RUIM :(");
        }
    }

    @Transactional
    public void trocarSenha(RecuperarSenhaDTO dto) {

        String email = dto.getEmail();
        String codigo = dto.getCodigo();
        String novaSenha = dto.getNovaSenha();

        Usuario usuario = repository.findByEmail(email).orElseThrow();

        if (usuario.getCodigoRecuperacao() == null || usuario.getCodigoRecuperacaoExpiracao() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma solicitação de recuperação foi feita.");
        }

        if (!usuario.getCodigoRecuperacao().equals(codigo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorreto.");
        }

        if (usuario.getCodigoRecuperacaoExpiracao().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado.");
        }

        String novaSenhaCriptografada = passwordEncoder.encode(novaSenha);
        usuario.setSenha(novaSenhaCriptografada);

        usuario.setCodigoRecuperacao(null);
        usuario.setCodigoRecuperacaoExpiracao(null);

        repository.save(usuario);
    }
}
