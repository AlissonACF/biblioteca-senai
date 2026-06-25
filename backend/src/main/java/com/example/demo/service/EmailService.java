package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmail(String destinatario, String titulo, String descricao) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("senai@participativo.com.br");
        helper.setTo(destinatario);
        helper.setSubject(titulo);
        helper.setText(descricao);

        mailSender.send(message);
    }

    public void enviarEmailFromTemplate(String destinatario, String titulo, String fileName) throws MessagingException{

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("senai@participativo.com.br");
        helper.setTo(destinatario);
        helper.setSubject(titulo);

        String filePath = "demo/src/main/resources/templates/email";

        filePath += fileName;

        Path path = Path.of(filePath);
        String html;
        try {
            html = Files.readString(path, StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erro ao ler template de email", e);
        }

        helper.setText(html, true);

        mailSender.send(message);
    }

}
