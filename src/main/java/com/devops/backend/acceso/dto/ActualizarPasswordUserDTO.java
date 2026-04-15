package com.devops.backend.acceso.dto;

public record ActualizarPasswordUserDTO(
    String passwordActual,
    String passwordNueva
) {}