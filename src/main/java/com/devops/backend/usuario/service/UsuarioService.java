package com.devops.backend.usuario.service;

import com.devops.backend.usuario.dto.*;
import com.devops.backend.usuario.entity.Usuario;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UsuarioService {

    SignUpResponseUsuario saveUser(SignUpUserRequest signupRequest);
    Usuario save(UsuarioDTO usuarioDTO);
    UserListResponse findById(Long id);
    UserListResponse findByDocumento(String documento);
    Page<UserListResponse> getAllUsers(UsuarioFilterRequest dtoFilter);
    UserListResponse updateUser(Long id, UpdateUsuarioRequest request);
    UserUpdateAdminResponse updateUserAdmin(Long id, UserUpdateAdminDto request);
    UserListResponse activar(Long id);
    UserListResponse desactivar(Long id);
    UserListResponse bloquear(Long id);
}