package cl.oyl.ventas.service;

import cl.oyl.ventas.dto.*;
import cl.oyl.ventas.exception.ResourceNotFoundException;
import cl.oyl.ventas.model.DetalleVenta;
import cl.oyl.ventas.model.EstadoVenta;
import cl.oyl.ventas.model.Venta;
import cl.oyl.ventas.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;

    public VentaServiceImpl(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    public VentaResponseDto crearVenta(VentaRequestDto dto) {
        Venta venta = new Venta();
        venta.setUsuarioId(dto.usuarioId());
        venta.setMetodoPago(dto.metodoPago());
        venta.setDireccionEntrega(dto.direccionEntrega());
        venta.setEstado(EstadoVenta.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVentaRequestDto detDto : dto.detalles()) {
            BigDecimal subtotal = detDto.precioUnitario().multiply(BigDecimal.valueOf(detDto.cantidad()));
            total = total.add(subtotal);

            DetalleVenta detalle = new DetalleVenta(
                detDto.productoId(),
                detDto.nombreProducto(),
                detDto.precioUnitario(),
                detDto.cantidad(),
                subtotal
            );
            venta.addDetalle(detalle);
        }

        venta.setTotal(total);

        Venta guardada = ventaRepository.save(venta);
        return mapToDto(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDto obtenerPorId(Long id) {
        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
        return mapToDto(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDto> listarPorUsuario(Long usuarioId) {
        return ventaRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId)
            .stream()
            .map(this::mapToDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDto> listarTodas() {
        return ventaRepository.findAll()
            .stream()
            .map(this::mapToDto)
            .toList();
    }

    @Override
    public VentaResponseDto cambiarEstado(Long id, CambioEstadoDto dto) {
        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
        venta.setEstado(dto.nuevoEstado());
        Venta actualizada = ventaRepository.save(venta);
        return mapToDto(actualizada);
    }

    private VentaResponseDto mapToDto(Venta v) {
        List<DetalleVentaResponseDto> detallesDto = v.getDetalles().stream()
            .map(d -> new DetalleVentaResponseDto(
                d.getId(),
                d.getProductoId(),
                d.getNombreProducto(),
                d.getPrecioUnitario(),
                d.getCantidad(),
                d.getSubtotal()
            )).toList();

        return new VentaResponseDto(
            v.getId(),
            v.getUsuarioId(),
            v.getTotal(),
            v.getEstado(),
            v.getMetodoPago(),
            v.getDireccionEntrega(),
            v.getCreatedAt(),
            detallesDto
        );
    }
}
