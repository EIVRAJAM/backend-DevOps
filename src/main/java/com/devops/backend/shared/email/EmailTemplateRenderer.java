package com.devops.backend.shared.email;

import com.samskivert.mustache.Mustache;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailTemplateRenderer {

    private final ResourceLoader resourceLoader;

    public String render(String templateName, Object model) {
        String path = "classpath:templates/email/" + templateName;

        try {
            Resource resource = resourceLoader.getResource(path);

            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return Mustache.compiler()
                        .compile(reader)
                        .execute(model);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo renderizar el template de correo: " + templateName, ex);
        }
    }
}
