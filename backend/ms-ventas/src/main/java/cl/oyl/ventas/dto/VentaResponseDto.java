package cl.oyl.ventas.dto;

import cl.oyl.ventas.model.EstadoVenta;
import cl.oyl.ventas.model.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponseDto(
    Long id,
    Long usuarioId,
    BigDecimal total,
    EstadoVenta estado,
    MetodoPago metodoPago,
    String direccionEntrega,
    LocalDateTime createdAt,
    List<DetalleVentaResponseDto> detalles
) {}
