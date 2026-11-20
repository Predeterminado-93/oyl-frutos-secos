package cl.oyl.ventas.repository;

import cl.oyl.ventas.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
}
