package cl.oyl.ventas.dto;

import cl.oyl.ventas.model.EstadoVenta;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoDto(
    @NotNull(message = "El nuevo estado es obligatorio")
    EstadoVenta nuevoEstado
) {}
