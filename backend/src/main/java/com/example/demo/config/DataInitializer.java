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
            usuarioRepository.findByEmail("admin@senai.br").ifPresentOrElse(
                admin -> {
                    admin.setNome(admin.getNome() == null || admin.getNome().isBlank() ? "Administrador SENAI" : admin.getNome());
                    admin.setNivelAcesso(NivelAcesso.ADMIN);
                    admin.setSenha(passwordEncoder.encode("admin123"));
                    usuarioRepository.save(admin);
                },
                () -> {
                    Usuario admin = new Usuario();
                    admin.setNome("Administrador SENAI");
                    admin.setEmail("admin@senai.br");
                    admin.setNivelAcesso(NivelAcesso.ADMIN);
                    admin.setSenha(passwordEncoder.encode("admin123"));
                    usuarioRepository.save(admin);
                    System.out.println("Usuario ADMIN criado: admin@senai.br / admin123");
                }
            );

            usuarioRepository.findByEmail("aluno@senai.br").ifPresentOrElse(
                aluno -> {
                    aluno.setNome(aluno.getNome() == null || aluno.getNome().isBlank() ? "Aluno Teste" : aluno.getNome());
                    aluno.setNivelAcesso(NivelAcesso.PADRAO);
                    aluno.setSenha(passwordEncoder.encode("aluno123"));
                    usuarioRepository.save(aluno);
                },
                () -> {
                    Usuario aluno = new Usuario();
                    aluno.setNome("Aluno Teste");
                    aluno.setEmail("aluno@senai.br");
                    aluno.setNivelAcesso(NivelAcesso.PADRAO);
                    aluno.setSenha(passwordEncoder.encode("aluno123"));
                    usuarioRepository.save(aluno);
                    System.out.println("Usuario aluno criado: aluno@senai.br / aluno123");
                }
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
            if (usuarioRepository.count() > 0) {
                return;
            }

            Usuario admin = new Usuario();
            admin.setNome("Administrador SENAI");
            admin.setEmail("admin@senai.br");
            admin.setNivelAcesso(NivelAcesso.ADMIN);
            admin.setSenha(passwordEncoder.encode("123456789"));
            usuarioRepository.save(admin);

            Usuario aluno = new Usuario();
            aluno.setNome("Aluno Teste");
            aluno.setEmail("aluno@senai.br");
            aluno.setNivelAcesso(NivelAcesso.PADRAO);
            aluno.setSenha(passwordEncoder.encode("user123"));
            usuarioRepository.save(aluno);
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
}
