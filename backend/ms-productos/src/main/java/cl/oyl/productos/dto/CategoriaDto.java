package cl.oyl.productos.dto;

public record CategoriaDto(
    Long id,
    String nombre,
    String descripcion,
    Boolean activo
) {}
