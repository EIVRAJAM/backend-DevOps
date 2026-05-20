package com.devops.backend.pago.dto;

import com.devops.backend.pago.enums.MedioReembolso;
import com.devops.backend.pago.enums.TipoCuentaReembolso;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CrearSolicitudReembolsoRequest {

    private String motivoSolicitud;

    private MedioReembolso medioReembolso;

    private String titularCuenta;

    private String documentoTitular;

    private String entidadFinanciera;

    private TipoCuentaReembolso tipoCuenta;

    private String numeroCuenta;

    private String correoContacto;

    private String telefonoContacto;

    private String observaciones;

    private MultipartFile certificadoCuenta;

    private MultipartFile documentoAdicional;
}
