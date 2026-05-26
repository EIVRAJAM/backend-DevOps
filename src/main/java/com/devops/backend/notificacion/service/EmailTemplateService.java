package com.devops.backend.notificacion.service;

import java.util.Map;

public interface EmailTemplateService {

    String renderizar(String nombrePlantilla, Map<String, Object> variables);

}
