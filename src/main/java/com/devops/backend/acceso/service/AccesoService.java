package com.devops.backend.acceso.service;

import com.devops.backend.acceso.entity.*;

import java.util.List;

public interface AccesoService {

    List<Acceso> findAll();
    Acceso save(Acceso acceso);
}