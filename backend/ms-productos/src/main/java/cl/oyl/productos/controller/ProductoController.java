package cl.oyl.productos.controller;

import cl.oyl.productos.dto.ProductoCreateDto;
import cl.oyl.productos.dto.ProductoDto;
import cl.oyl.productos.dto.StockUpdateDto;
import cl.oyl.productos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<ProductoDto> crear(@Valid @RequestBody ProductoCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductoDto>> listarActivos() {
        return ResponseEntity.ok(productoService.listarActivos());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoDto>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(productoService.listarPorCategoria(categoriaId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoDto>> buscar(@RequestParam("q") String query) {
        return ResponseEntity.ok(productoService.buscarPorNombre(query));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductoDto> actualizarStock(@PathVariable Long id, @Valid @RequestBody StockUpdateDto dto) {
        return ResponseEntity.ok(productoService.actualizarStock(id, dto));
    }
}
