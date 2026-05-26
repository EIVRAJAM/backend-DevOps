package com.devops.backend.acceso.mappers;

import com.devops.backend.acceso.dto.AccesoAdminDTO;
import com.devops.backend.acceso.dto.AccesoUserDTO;
import com.devops.backend.acceso.entity.Acceso;
import org.springframework.stereotype.Component;

@Component
public class AccesoMapper {

    public AccesoAdminDTO toAccesoAdminDTO(Acceso acceso) {
        if (acceso == null) {
            return null;
        }

        return new AccesoAdminDTO(
                acceso.getIdUsuario(),
                acceso.getUsername(),
                acceso.getCorreoAcceso(),
                acceso.getIntentosFallidos(),
                acceso.getEstadoCuenta(),
                acceso.getUuidAcceso(),
                acceso.getUltimoLogin(),
                acceso.getCreadoEn(),
                acceso.getActualizadoEn());
    }

    public AccesoUserDTO toAccesoUserDTO(Acceso acceso) {
        if (acceso == null) {
            return null;
        }

        return new AccesoUserDTO(
                acceso.getUsername(),
                acceso.getCorreoAcceso());
    }
}
