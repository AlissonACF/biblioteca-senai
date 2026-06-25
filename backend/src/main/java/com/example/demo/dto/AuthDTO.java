package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthDTO {

    /**
     * Pode ser email (admin) ou CPF com 11 dígitos (usuário comum).
     */
    @NotBlank(message = "O identificador deve ser preenchido.")
    private String email;

    @NotBlank(message = "A senha deve ser preenchida.")
    private String senha;
}
