package cl.oyl.ventas.service;

import cl.oyl.ventas.dto.CambioEstadoDto;
import cl.oyl.ventas.dto.VentaRequestDto;
import cl.oyl.ventas.dto.VentaResponseDto;

import java.util.List;

public interface VentaService {
    VentaResponseDto crearVenta(VentaRequestDto dto);
    VentaResponseDto obtenerPorId(Long id);
    List<VentaResponseDto> listarPorUsuario(Long usuarioId);
    List<VentaResponseDto> listarTodas();
    VentaResponseDto cambiarEstado(Long id, CambioEstadoDto dto);
}
