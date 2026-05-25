package com.devops.backend.usuario.service;

import com.devops.backend.usuario.dto.*;
import com.devops.backend.usuario.entity.Usuario;
import org.springframework.data.domain.Page;


public interface UsuarioService {

    SignUpResponseUsuario saveUser(SignUpUserRequest signupRequest);
    Usuario save(UsuarioDTO usuarioDTO);
    UserResponseAdmin findById(Long id);
    UserResponseAdmin findByDocumento(String documento);
    Page<UserListResponse> getAllUsers(UsuarioFilterRequest dtoFilter);
    UserListResponse updateUser(Long id, UpdateUsuarioRequest request);
    UserUpdateAdminResponse updateUserAdmin(Long id, UserUpdateAdminDto request);
    UserResponseAdmin activar(Long id);
    UserResponseAdmin desactivar(Long id);
    UserResponseAdmin bloquear(Long id);
    UserListResponse findByIdUser(Long idUsuario);
    CompleteStatusResponse getCompleteStatus(Long userId);
    UsuarioOrganizadorDTO findByIdOrganizador(Long id);
    Page<UsuarioOrganizadorDTO> buscarUsuariosOrganizador(UsuarioFilterRequest filter, String username, String correo);
}