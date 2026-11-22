package cl.oyl.ventas.dto;

import cl.oyl.ventas.model.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record VentaRequestDto(
    @NotNull(message = "El id de usuario es obligatorio")
    Long usuarioId,

    @NotNull(message = "El método de pago es obligatorio")
    MetodoPago metodoPago,

    String direccionEntrega,

    @NotEmpty(message = "La venta debe contener al menos un producto")
    @Valid
    List<DetalleVentaRequestDto> detalles
) {}
