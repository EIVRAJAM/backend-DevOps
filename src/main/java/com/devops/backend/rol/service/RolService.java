package com.devops.backend.rol.service;

import com.devops.backend.rol.entity.dto.RolFilterRequest;
import com.devops.backend.rol.entity.dto.RolRequest;
import com.devops.backend.rol.entity.dto.RolResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RolService {

    RolResponse save(RolRequest request);
    List<RolResponse> findAll();
    Page<RolResponse> findAllFilter(RolFilterRequest filter);
    RolResponse findById(Long id); // <-- nuevo
    RolResponse findByName(String name); // <-- nuevo

}
