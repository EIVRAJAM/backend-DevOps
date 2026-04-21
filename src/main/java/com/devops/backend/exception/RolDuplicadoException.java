package com.devops.backend.exception;

public class RolDuplicadoException extends RuntimeException{

    public RolDuplicadoException(String nombreRol) {
        super("Ya existe un rol con el nombre: " + nombreRol);
    }

}
