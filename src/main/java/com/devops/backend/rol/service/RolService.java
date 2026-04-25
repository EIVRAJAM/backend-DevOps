package com.devops.backend.rol.service;

import com.devops.backend.rol.entity.dto.RolRequest;
import com.devops.backend.rol.entity.dto.RolResponse;

import java.util.List;

public interface RolService {

    RolResponse save(RolRequest request);
    List<RolResponse> findAll();
}
