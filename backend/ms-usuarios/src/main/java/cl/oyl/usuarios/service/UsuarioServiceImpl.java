package cl.oyl.usuarios.service;

import cl.oyl.usuarios.dto.LoginResponseDto;
import cl.oyl.usuarios.dto.UsuarioLoginDto;
import cl.oyl.usuarios.dto.UsuarioRegistroDto;
import cl.oyl.usuarios.dto.UsuarioResponseDto;
import cl.oyl.usuarios.exception.BadRequestException;
import cl.oyl.usuarios.exception.ResourceNotFoundException;
import cl.oyl.usuarios.model.Rol;
import cl.oyl.usuarios.model.Usuario;
import cl.oyl.usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponseDto registrar(UsuarioRegistroDto dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new BadRequestException("El email '" + dto.email() + "' ya se encuentra registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.nombre());
        usuario.setApellido(dto.apellido());
        usuario.setEmail(dto.email());
        usuario.setPassword(passwordEncoder.encode(dto.password()));
        usuario.setTelefono(dto.telefono());
        usuario.setDireccion(dto.direccion());
        usuario.setRol(Rol.ROLE_CLIENTE);
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        return mapToResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(UsuarioLoginDto dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
            .orElseThrow(() -> new BadRequestException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(dto.password(), usuario.getPassword())) {
            throw new BadRequestException("Credenciales incorrectas");
        }

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new BadRequestException("La cuenta de usuario se encuentra desactivada");
        }

        // Estructura lista para token JWT (representación bearer token con payload base64)
        String mockJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("{\"sub\":\"" + usuario.getEmail() + "\",\"id\":" + usuario.getId() + ",\"role\":\"" + usuario.getRol().name() + "\"}").getBytes()
            ) + "." + UUID.randomUUID().toString().replace("-", "");

        return new LoginResponseDto(mockJwtToken, "Bearer", mapToResponse(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDto obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario con id " + id + " no encontrado"));
        return mapToResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDto obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario con email " + email + " no encontrado"));
        return mapToResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> listarTodos() {
        return usuarioRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    private UsuarioResponseDto mapToResponse(Usuario usuario) {
        return new UsuarioResponseDto(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            usuario.getTelefono(),
            usuario.getDireccion(),
            usuario.getRol(),
            usuario.getActivo()
        );
    }
}
