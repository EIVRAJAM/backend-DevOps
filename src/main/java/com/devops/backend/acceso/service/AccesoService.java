package com.devops.backend.acceso.service;

import com.devops.backend.acceso.dto.AccesoUserDTO;
import com.devops.backend.acceso.entity.*;

import java.util.List;
import java.util.Optional;

public interface AccesoService {

    List<Acceso> findAll();

    Optional<Acceso> findByIdUsuario(Long idUsuario);

    Acceso save(Acceso acceso);

    Acceso saveWithDefaultPassword(Long idUsuario, String username, String correoAcceso);

    Acceso update(Long idUsuario, AccesoUserDTO accesoUserDTO);

    Acceso desactivarCuenta(Long idUsuario);
}