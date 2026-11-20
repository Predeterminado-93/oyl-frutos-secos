package cl.oyl.usuarios.dto;

import cl.oyl.usuarios.model.Rol;

public record UsuarioResponseDto(
    Long id,
    String nombre,
    String apellido,
    String email,
    String telefono,
    String direccion,
    Rol rol,
    Boolean activo
) {}
