package cl.oyl.ventas.controller;

import cl.oyl.ventas.dto.CambioEstadoDto;
import cl.oyl.ventas.dto.VentaRequestDto;
import cl.oyl.ventas.dto.VentaResponseDto;
import cl.oyl.ventas.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<VentaResponseDto> crearVenta(@Valid @RequestBody VentaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.crearVenta(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<VentaResponseDto>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(ventaService.listarPorUsuario(usuarioId));
    }

    @GetMapping
    public ResponseEntity<List<VentaResponseDto>> listarTodas() {
        return ResponseEntity.ok(ventaService.listarTodas());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<VentaResponseDto> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoDto dto) {
        return ResponseEntity.ok(ventaService.cambiarEstado(id, dto));
    }
}
