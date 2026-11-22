package cl.oyl.productos.service;

import cl.oyl.productos.dto.CategoriaCreateDto;
import cl.oyl.productos.dto.CategoriaDto;
import cl.oyl.productos.exception.ResourceNotFoundException;
import cl.oyl.productos.model.Categoria;
import cl.oyl.productos.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public CategoriaDto crear(CategoriaCreateDto dto) {
        Categoria categoria = new Categoria(dto.nombre(), dto.descripcion());
        Categoria guardada = categoriaRepository.save(categoria);
        return new CategoriaDto(guardada.getId(), guardada.getNombre(), guardada.getDescripcion(), guardada.getActivo());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDto obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        return new CategoriaDto(categoria.getId(), categoria.getNombre(), categoria.getDescripcion(), categoria.getActivo());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDto> listarActivas() {
        return categoriaRepository.findByActivoTrue()
            .stream()
            .map(c -> new CategoriaDto(c.getId(), c.getNombre(), c.getDescripcion(), c.getActivo()))
            .toList();
    }
}
