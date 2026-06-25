package com.example.demo.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckinDTO {

    @NotNull(message = "O ID do posto é obrigatório")
    private Long postoId;

    private MultipartFile foto;

}
