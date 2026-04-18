package com.devops.backend.funcionalidad.dto;

public record FuncionalidadFilterRequest(
        String estado,
        Long id_padre) { }
