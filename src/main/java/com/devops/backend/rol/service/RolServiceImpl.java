package com.devops.backend.rol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devops.backend.rol.repository.RolRepository;

@Service
public class RolServiceImpl implements RolService {
    @Autowired
    private RolRepository rolRepository;
}
