package cl.oyl.ventas.service;

import cl.oyl.ventas.dto.DetalleVentaRequestDto;
import cl.oyl.ventas.dto.VentaRequestDto;
import cl.oyl.ventas.dto.VentaResponseDto;
import cl.oyl.ventas.model.EstadoVenta;
import cl.oyl.ventas.model.MetodoPago;
import cl.oyl.ventas.model.Venta;
import cl.oyl.ventas.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Venta ventaPrueba;

    @BeforeEach
    void setUp() {
        ventaPrueba = new Venta();
        ventaPrueba.setId(100L);
        ventaPrueba.setUsuarioId(5L);
        ventaPrueba.setTotal(new BigDecimal("15000.00"));
        ventaPrueba.setEstado(EstadoVenta.PENDIENTE);
        ventaPrueba.setMetodoPago(MetodoPago.TARJETA_DEBITO);
        ventaPrueba.setDireccionEntrega("Calle Los Alerces 456");
    }

    @Test
    @DisplayName("Debe crear una venta y calcular el total correctamente")
    void crearVentaExitoso() {
        DetalleVentaRequestDto det1 = new DetalleVentaRequestDto(
            1L, "Nueces Mariposa 500g", new BigDecimal("5000.00"), 2
        );
        DetalleVentaRequestDto det2 = new DetalleVentaRequestDto(
            2L, "Castañas de Cajú 250g", new BigDecimal("5000.00"), 1
        );

        VentaRequestDto requestDto = new VentaRequestDto(
            5L,
            MetodoPago.TARJETA_DEBITO,
            "Calle Los Alerces 456",
            List.of(det1, det2)
        );

        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta v = invocation.getArgument(0);
            v.setId(100L);
            return v;
        });

        VentaResponseDto response = ventaService.crearVenta(requestDto);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(5L, response.usuarioId());
        assertEquals(new BigDecimal("15000.00"), response.total());
        assertEquals(EstadoVenta.PENDIENTE, response.estado());
        assertEquals(2, response.detalles().size());
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("Debe obtener una venta existente por su ID")
    void obtenerVentaPorIdExitoso() {
        when(ventaRepository.findById(100L)).thenReturn(Optional.of(ventaPrueba));

        VentaResponseDto response = ventaService.obtenerPorId(100L);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(new BigDecimal("15000.00"), response.total());
    }
}
