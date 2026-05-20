package com.devops.backend.pago.service;

import com.devops.backend.pago.dto.CrearSolicitudReembolsoRequest;
import com.devops.backend.pago.enums.MedioReembolso;
import com.devops.backend.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Component
public class ReembolsoRequestValidator {

    private static final List<String> MIME_PERMITIDOS = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/jpg"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public void validar(CrearSolicitudReembolsoRequest request) {
        if (request.getMotivoSolicitud() == null || request.getMotivoSolicitud().isBlank()) {
            throw new BadRequestException("El motivo de la solicitud es obligatorio");
        }
        if (request.getMotivoSolicitud().length() < 10) {
            throw new BadRequestException("El motivo debe tener al menos 10 caracteres");
        }
        if (request.getMotivoSolicitud().length() > 500) {
            throw new BadRequestException("El motivo no puede exceder 500 caracteres");
        }
        if (request.getMedioReembolso() == null) {
            throw new BadRequestException("El medio de reembolso es obligatorio");
        }
        if (request.getTitularCuenta() == null || request.getTitularCuenta().isBlank()) {
            throw new BadRequestException("El titular de la cuenta es obligatorio");
        }
        if (request.getDocumentoTitular() == null || request.getDocumentoTitular().isBlank()) {
            throw new BadRequestException("El documento del titular es obligatorio");
        }
        if (request.getCorreoContacto() == null || request.getCorreoContacto().isBlank()) {
            throw new BadRequestException("El correo de contacto es obligatorio");
        }
        if (!request.getCorreoContacto().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("El correo de contacto no tiene un formato valido");
        }
        if (request.getTelefonoContacto() == null || request.getTelefonoContacto().isBlank()) {
            throw new BadRequestException("El telefono de contacto es obligatorio");
        }

        if (request.getMedioReembolso() == MedioReembolso.CUENTA_BANCARIA) {
            if (request.getEntidadFinanciera() == null || request.getEntidadFinanciera().isBlank()) {
                throw new BadRequestException("La entidad financiera es obligatoria para cuentas bancarias");
            }
            if (request.getTipoCuenta() == null) {
                throw new BadRequestException("El tipo de cuenta es obligatorio para cuentas bancarias");
            }
            if (request.getNumeroCuenta() == null || request.getNumeroCuenta().isBlank()) {
                throw new BadRequestException("El numero de cuenta es obligatorio para cuentas bancarias");
            }
            if (request.getCertificadoCuenta() == null || request.getCertificadoCuenta().isEmpty()) {
                throw new BadRequestException("El certificado de cuenta es obligatorio para cuentas bancarias");
            }
        }

        validarArchivo(request.getCertificadoCuenta(), "certificado de cuenta");
        validarArchivo(request.getDocumentoAdicional(), "documento adicional");
    }

    private void validarArchivo(MultipartFile file, String nombreCampo) {
        if (file == null || file.isEmpty()) return;

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                    "El " + nombreCampo + " excede el tamano maximo de 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !MIME_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                    "El " + nombreCampo + " debe ser PDF, JPG o PNG. Tipo recibido: " + contentType);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String lower = originalFilename.toLowerCase();
            if (lower.endsWith(".zip") || lower.endsWith(".exe") || lower.endsWith(".rar")
                    || lower.endsWith(".docm") || lower.endsWith(".xlsm") || lower.endsWith(".pptm")) {
                throw new BadRequestException(
                        "El tipo de archivo del " + nombreCampo + " no esta permitido: " + originalFilename);
            }
        }
    }
}
