package cl.oyl.usuarios.dto;

public record LoginResponseDto(
    String token,
    String tipo,
    UsuarioResponseDto usuario
) {}
