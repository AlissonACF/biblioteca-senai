package com.example.demo.dto;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "O ID do posto e obrigatorio.")
    private Long postoId;

    @NotNull(message = "As prevencoes devem ser preenchidas.")
    @Min(value = 0, message = "As prevencoes nao podem ser negativas.")
    private Integer prevencoes;

    @NotNull(message = "As lesoes devem ser preenchidas.")
    @Min(value = 0, message = "As lesoes nao podem ser negativas.")
    private Integer lesoes;

    @JsonIgnore
    private MultipartFile foto;
}
