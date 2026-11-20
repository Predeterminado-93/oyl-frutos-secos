package cl.oyl.productos.service;

import cl.oyl.productos.dto.ProductoCreateDto;
import cl.oyl.productos.dto.ProductoDto;
import cl.oyl.productos.dto.StockUpdateDto;

import java.util.List;

public interface ProductoService {
    ProductoDto crear(ProductoCreateDto dto);
    ProductoDto obtenerPorId(Long id);
    List<ProductoDto> listarActivos();
    List<ProductoDto> listarPorCategoria(Long categoriaId);
    List<ProductoDto> buscarPorNombre(String query);
    ProductoDto actualizarStock(Long id, StockUpdateDto dto);
}
