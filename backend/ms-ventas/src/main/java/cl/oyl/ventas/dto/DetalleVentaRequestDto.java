package cl.oyl.ventas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DetalleVentaRequestDto(
    @NotNull(message = "El id del producto es obligatorio")
    Long productoId,

    @NotBlank(message = "El nombre del producto es obligatorio")
    String nombreProducto,

    @NotNull(message = "El precio unitario es obligatorio")
    @Min(value = 0, message = "El precio unitario debe ser mayor o igual a 0")
    BigDecimal precioUnitario,

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    Integer cantidad
) {}
