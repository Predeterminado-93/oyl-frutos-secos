package cl.oyl.productos.service;

import cl.oyl.productos.dto.CategoriaCreateDto;
import cl.oyl.productos.dto.CategoriaDto;

import java.util.List;

public interface CategoriaService {
    CategoriaDto crear(CategoriaCreateDto dto);
    CategoriaDto obtenerPorId(Long id);
    List<CategoriaDto> listarActivas();
}
