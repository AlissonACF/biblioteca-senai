package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.example.demo.enums.PostoStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostoDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome deve ser preenchido.")
    private String nome;

    private String descricao;


    private PostoStatus status = PostoStatus.DISPONIVEL;

}
