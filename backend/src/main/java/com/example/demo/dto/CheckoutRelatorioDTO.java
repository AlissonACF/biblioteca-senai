package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CheckoutRelatorioDTO {
    private Long id;
    private Long postoId;
    private String postoNome;
    private String guardaVidasEmail;
    private Integer prevencoes;
    private Integer lesoes;
    private LocalDateTime horario;
    private String fotoNome;
}
