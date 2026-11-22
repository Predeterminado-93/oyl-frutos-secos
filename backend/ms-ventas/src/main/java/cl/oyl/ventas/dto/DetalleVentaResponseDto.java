package cl.oyl.ventas.dto;

import java.math.BigDecimal;

public record DetalleVentaResponseDto(
    Long id,
    Long productoId,
    String nombreProducto,
    BigDecimal precioUnitario,
    Integer cantidad,
    BigDecimal subtotal
) {}
