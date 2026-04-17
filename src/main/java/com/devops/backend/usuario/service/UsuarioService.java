package com.devops.backend.usuario.service;

import com.devops.backend.auth.dto.SignUpRequest;
import com.devops.backend.usuario.dto.*;
import com.devops.backend.usuario.entity.Usuario;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    SignUpResponseUsuario saveUser(SignUpRequest signupRequest);
    Usuario save(UsuarioDTO usuarioDTO);
    UserListResponse findById(Long id);
    UserListResponse findByDocumento(String documento);
    List<UserListResponse> getAllUsers();
    Page<UserListResponse> getAllUsers(UsuarioFilterRequest dtoFilter);
    UserListResponse updateUser(Long id, UpdateUsuarioRequest request);

}