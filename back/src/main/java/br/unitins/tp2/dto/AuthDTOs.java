package br.unitins.tp2.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthDTOs {
    private AuthDTOs() {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record LogoutRequest(@NotBlank String refreshToken) {}
    public record RegistroRequest(@NotBlank @Size(min = 15, max = 128) String senha,
            @NotNull @Valid ClienteRequestDTO cliente) {}
    public record UsuarioAuth(Long id, String nome, String email, List<String> roles, ClienteResponseDTO cliente) {}
    public record TokenResponse(String accessToken, String refreshToken, long expiresIn, UsuarioAuth usuario) {}
}
