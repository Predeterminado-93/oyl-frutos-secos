package cl.oyl.productos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockUpdateDto(
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    Integer cantidad,

    @NotNull(message = "Debe indicar la operación: 'DESCONTAR' o 'REPONER'")
    String operacion
) {}
