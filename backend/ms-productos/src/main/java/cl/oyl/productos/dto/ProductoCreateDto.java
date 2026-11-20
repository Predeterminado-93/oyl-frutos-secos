package cl.oyl.productos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductoCreateDto(
    @NotBlank(message = "El nombre del producto es obligatorio")
    String nombre,

    String descripcion,

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio debe ser mayor o igual a 0")
    BigDecimal precio,

    @NotNull(message = "El stock inicial es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    Integer stock,

    String imagenUrl,

    @NotNull(message = "El id de categoría es obligatorio")
    Long categoriaId
) {}
