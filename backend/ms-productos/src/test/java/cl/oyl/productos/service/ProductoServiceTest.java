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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Categoria categoriaPrueba;
    private Producto productoPrueba;

    @BeforeEach
    void setUp() {
        categoriaPrueba = new Categoria("Frutos Secos", "Variedad de nueces y almendras");
        categoriaPrueba.setId(1L);

        productoPrueba = new Producto();
        productoPrueba.setId(10L);
        productoPrueba.setNombre("Almendras tostadas 500g");
        productoPrueba.setDescripcion("Almendras seleccionadas");
        productoPrueba.setPrecio(new BigDecimal("7500.00"));
        productoPrueba.setStock(20);
        productoPrueba.setCategoria(categoriaPrueba);
        productoPrueba.setActivo(true);
    }

    @Test
    @DisplayName("Debe crear un producto correctamente asociado a una categoría existente")
    void crearProductoExitoso() {
        ProductoCreateDto createDto = new ProductoCreateDto(
            "Almendras tostadas 500g",
            "Almendras seleccionadas",
            new BigDecimal("7500.00"),
            20,
            "http://example.com/almendra.jpg",
            1L
        );

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaPrueba));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoPrueba);

        ProductoDto response = productoService.crear(createDto);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Almendras tostadas 500g", response.nombre());
        assertEquals(1L, response.categoriaId());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe descontar stock correctamente cuando hay suficiente disponibilidad")
    void descontarStockExitoso() {
        StockUpdateDto stockDto = new StockUpdateDto(5, "DESCONTAR");
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoPrueba));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductoDto response = productoService.actualizarStock(10L, stockDto);

        assertEquals(15, response.stock());
    }

    @Test
    @DisplayName("Debe lanzar InsufficientStockException si se solicita más del stock disponible")
    void descontarStockInsuficiente() {
        StockUpdateDto stockDto = new StockUpdateDto(50, "DESCONTAR");
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoPrueba));

        assertThrows(InsufficientStockException.class, () -> productoService.actualizarStock(10L, stockDto));
        verify(productoRepository, never()).save(any(Producto.class));
    }
}
