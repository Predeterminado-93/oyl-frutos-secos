package cl.oyl.usuarios.service;

import cl.oyl.usuarios.dto.LoginResponseDto;
import cl.oyl.usuarios.dto.UsuarioLoginDto;
import cl.oyl.usuarios.dto.UsuarioRegistroDto;
import cl.oyl.usuarios.dto.UsuarioResponseDto;

import java.util.List;

public interface UsuarioService {
    UsuarioResponseDto registrar(UsuarioRegistroDto dto);
    LoginResponseDto login(UsuarioLoginDto dto);
    UsuarioResponseDto obtenerPorId(Long id);
    UsuarioResponseDto obtenerPorEmail(String email);
    List<UsuarioResponseDto> listarTodos();
}
