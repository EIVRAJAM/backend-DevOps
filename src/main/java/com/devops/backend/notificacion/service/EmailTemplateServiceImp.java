package com.devops.backend.notificacion.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import org.thymeleaf.context.Context;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImp implements EmailTemplateService {


    private final TemplateEngine templateEngine;

    @Override
    public String renderizar(String nombrePlantilla, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(nombrePlantilla, context);
    }


}
