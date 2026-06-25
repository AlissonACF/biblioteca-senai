package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import com.example.demo.entity.Posto;
import com.example.demo.entity.RecursoBiblioteca;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.enums.PostoStatus;
import com.example.demo.enums.ResourceKind;
import com.example.demo.repository.RecursoBibliotecaRepository;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initDatabase(
        UsuarioRepository usuarioRepository,
        RecursoBibliotecaRepository recursoRepository
    ) {
        return args -> {
            upsertDefaultUser(
                usuarioRepository,
                "admin@senai.br",
                "Administrador SENAI",
                "admin123",
                NivelAcesso.ADMIN
            );

            upsertDefaultUser(
                usuarioRepository,
                "aluno@senai.br",
                "Aluno Teste",
                "aluno123",
                NivelAcesso.PADRAO
            );

            if (recursoRepository != null && recursoRepository.findAll().isEmpty()) {
                for (int i = 1; i <= 4; i++) {
                    RecursoBiblioteca sala = new RecursoBiblioteca();
                    sala.setKind(ResourceKind.room);
                    sala.setName("Sala de Estudo " + i);
                    sala.setCapacity(5);
                    recursoRepository.save(sala);
                }

                for (int i = 1; i <= 9; i++) {
                    RecursoBiblioteca computador = new RecursoBiblioteca();
                    computador.setKind(ResourceKind.computer);
                    computador.setName("Computador " + String.format("%02d", i));
                    computador.setCapacity(2);
                    recursoRepository.save(computador);
                }

                System.out.println("Recursos iniciais da biblioteca criados.");
            }
        };
    }

    public CommandLineRunner initDatabase(UsuarioRepository usuarioRepository) {
        return args -> {
            upsertDefaultUser(
                usuarioRepository,
                "admin@senai.br",
                "Administrador SENAI",
                "admin123",
                NivelAcesso.ADMIN
            );

            upsertDefaultUser(
                usuarioRepository,
                "aluno@senai.br",
                "Aluno Teste",
                "aluno123",
                NivelAcesso.PADRAO
            );
        };
    }

    public CommandLineRunner initPostos(PostoRepository postoRepository) {
        return args -> {
            List<Posto> postos = postoRepository.findTodosOrderByIdAsc();
            for (Posto posto : postos) {
                int numero = posto.getId() == null ? postos.indexOf(posto) + 1 : posto.getId().intValue();
                posto.setNome("Posto " + numero);
                posto.setDescricao("Posto de guarda-vidas " + numero);
                posto.setStatus(PostoStatus.DISPONIVEL);
                posto.setAtivo(true);
            }
            if (!postos.isEmpty()) {
                postoRepository.saveAll(postos);
            }

            int faltantes = 21 - postos.size();
            for (int i = 0; i < faltantes; i++) {
                int numero = postos.size() + i + 1;
                Posto posto = new Posto();
                posto.setNome("Posto " + numero);
                posto.setDescricao("Posto de guarda-vidas " + numero);
                posto.setStatus(PostoStatus.DISPONIVEL);
                posto.setAtivo(true);
                postoRepository.save(posto);
            }
        };
    }

    private void upsertDefaultUser(
        UsuarioRepository usuarioRepository,
        String email,
        String nome,
        String senha,
        NivelAcesso nivelAcesso
    ) {
        Usuario usuario = usuarioRepository.findAnyByEmail(email).orElseGet(Usuario::new);
        boolean novo = usuario.getId() == null;

        usuario.setEmail(email);
        usuario.setNome(usuario.getNome() == null || usuario.getNome().isBlank() ? nome : usuario.getNome());
        usuario.setNivelAcesso(nivelAcesso);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setAtivo(true);
        usuario.setDeletedAt(null);

        usuarioRepository.save(usuario);

        if (novo) {
            System.out.println("Usuario criado: " + email + " / " + senha);
        } else {
            System.out.println("Usuario padrao atualizado: " + email + " / " + senha);
        }
    }
}
