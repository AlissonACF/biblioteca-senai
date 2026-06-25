package com.example.demo.service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Arquivo;
import com.example.demo.repository.ArquivoRepository;

@Service
public class ArquivoService {

    @Value("${arquivamento.path}")
    private String path;

    @Autowired
    private ArquivoRepository arquivoRepository;

    public Arquivo upload(MultipartFile file) {
        Path root = Paths.get(path);

        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            String nome = UUID.randomUUID().toString();

            Path destino = root.resolve(nome);

            System.out.println(destino);

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            Arquivo arquivo = new Arquivo();

            arquivo.setCaminho(destino.toString());
            arquivo.setNome(file.getOriginalFilename());
            arquivo.setTamanho(file.getSize());
            arquivo.setTipo(file.getContentType());


            return arquivoRepository.save(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar o arquivo", e);
        }

    }

}
