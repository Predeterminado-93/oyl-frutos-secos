package cl.oyl.productos.service;

import cl.oyl.productos.dto.ProductoCreateDto;
import cl.oyl.productos.dto.ProductoDto;
import cl.oyl.productos.dto.StockUpdateDto;
import cl.oyl.productos.exception.InsufficientStockException;
import cl.oyl.productos.exception.ResourceNotFoundException;
import cl.oyl.productos.model.Categoria;
import cl.oyl.productos.model.Producto;
import cl.oyl.productos.repository.CategoriaRepository;
import cl.oyl.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public ProductoDto crear(ProductoCreateDto dto) {
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + dto.categoriaId()));

        Producto producto = new Producto();
        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setPrecio(dto.precio());
        producto.setStock(dto.stock());
        producto.setImagenUrl(dto.imagenUrl());
        producto.setCategoria(categoria);
        producto.setActivo(true);

        Producto guardado = productoRepository.save(producto);
        return mapToDto(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDto obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        return mapToDto(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> listarActivos() {
        return productoRepository.findByActivoTrue().stream()
            .map(this::mapToDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> listarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaIdAndActivoTrue(categoriaId).stream()
            .map(this::mapToDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> buscarPorNombre(String query) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(query).stream()
            .map(this::mapToDto)
            .toList();
    }

    @Override
    public ProductoDto actualizarStock(Long id, StockUpdateDto dto) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));

        if ("DESCONTAR".equalsIgnoreCase(dto.operacion())) {
            if (producto.getStock() < dto.cantidad()) {
                throw new InsufficientStockException("Stock insuficiente para '" + producto.getNombre() + "'. Disponible: " + producto.getStock() + ", Solicitado: " + dto.cantidad());
            }
            producto.setStock(producto.getStock() - dto.cantidad());
        } else if ("REPONER".equalsIgnoreCase(dto.operacion())) {
            producto.setStock(producto.getStock() + dto.cantidad());
        } else {
            throw new IllegalArgumentException("Operación no soportada: " + dto.operacion());
        }

        Producto actualizado = productoRepository.save(producto);
        return mapToDto(actualizado);
    }

    private ProductoDto mapToDto(Producto p) {
        Long catId = p.getCategoria() != null ? p.getCategoria().getId() : null;
        String catNombre = p.getCategoria() != null ? p.getCategoria().getNombre() : null;
        return new ProductoDto(
            p.getId(),
            p.getNombre(),
            p.getDescripcion(),
            p.getPrecio(),
            p.getStock(),
            p.getImagenUrl(),
            catId,
            catNombre,
            p.getActivo()
        );
    }
}
