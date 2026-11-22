package cl.oyl.productos.dto;

import java.math.BigDecimal;

public record ProductoDto(
    Long id,
    String nombre,
    String descripcion,
    BigDecimal precio,
    Integer stock,
    String imagenUrl,
    Long categoriaId,
    String categoriaNombre,
    Boolean activo
) {}
