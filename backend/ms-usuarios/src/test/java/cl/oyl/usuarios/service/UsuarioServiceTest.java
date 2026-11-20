package cl.oyl.usuarios.service;

import cl.oyl.usuarios.dto.LoginResponseDto;
import cl.oyl.usuarios.dto.UsuarioLoginDto;
import cl.oyl.usuarios.dto.UsuarioRegistroDto;
import cl.oyl.usuarios.dto.UsuarioResponseDto;
import cl.oyl.usuarios.exception.BadRequestException;
import cl.oyl.usuarios.model.Rol;
import cl.oyl.usuarios.model.Usuario;
import cl.oyl.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setNombre("Juan");
        usuarioPrueba.setApellido("Perez");
        usuarioPrueba.setEmail("juan@oyl.cl");
        usuarioPrueba.setPassword("encodedPassword");
        usuarioPrueba.setRol(Rol.ROLE_CLIENTE);
        usuarioPrueba.setActivo(true);
    }

    @Test
    @DisplayName("Debe registrar un nuevo usuario exitosamente")
    void registrarUsuarioExitoso() {
        UsuarioRegistroDto registroDto = new UsuarioRegistroDto(
            "Juan", "Perez", "juan@oyl.cl", "123456", "+56912345678", "Av. Principal 123"
        );

        when(usuarioRepository.existsByEmail("juan@oyl.cl")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioPrueba);

        UsuarioResponseDto respuesta = usuarioService.registrar(registroDto);

        assertNotNull(respuesta);
        assertEquals(1L, respuesta.id());
        assertEquals("juan@oyl.cl", respuesta.email());
        assertEquals(Rol.ROLE_CLIENTE, respuesta.rol());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe fallar al registrar si el email ya existe")
    void registrarUsuarioEmailDuplicado() {
        UsuarioRegistroDto registroDto = new UsuarioRegistroDto(
            "Juan", "Perez", "juan@oyl.cl", "123456", null, null
        );

        when(usuarioRepository.existsByEmail("juan@oyl.cl")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> usuarioService.registrar(registroDto));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe iniciar sesión correctamente con credenciales válidas")
    void loginExitoso() {
        UsuarioLoginDto loginDto = new UsuarioLoginDto("juan@oyl.cl", "123456");

        when(usuarioRepository.findByEmail("juan@oyl.cl")).thenReturn(Optional.of(usuarioPrueba));
        when(passwordEncoder.matches("123456", "encodedPassword")).thenReturn(true);

        LoginResponseDto response = usuarioService.login(loginDto);

        assertNotNull(response);
        assertNotNull(response.token());
        assertEquals("Bearer", response.tipo());
        assertEquals("juan@oyl.cl", response.usuario().email());
    }

    @Test
    @DisplayName("Debe fallar login con contraseña incorrecta")
    void loginPasswordIncorrecto() {
        UsuarioLoginDto loginDto = new UsuarioLoginDto("juan@oyl.cl", "passwordErroneo");

        when(usuarioRepository.findByEmail("juan@oyl.cl")).thenReturn(Optional.of(usuarioPrueba));
        when(passwordEncoder.matches("passwordErroneo", "encodedPassword")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> usuarioService.login(loginDto));
    }
}
