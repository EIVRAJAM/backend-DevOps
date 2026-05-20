package com.devops.backend.shared.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class EmailAttachmentStorageService {

    private final Path baseDir;

    public EmailAttachmentStorageService(
            @Value("${app.email.attachments.temp-dir:${java.io.tmpdir}/devops-email-attachments}") String tempDir) {
        this.baseDir = Paths.get(tempDir);
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear directorio de adjuntos: " + baseDir, e);
        }
    }

    public StoredAttachment store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        String originalName = sanitizeFilename(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "_" + originalName;
        Path targetPath = baseDir.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar archivo adjunto: " + originalName, e);
        }

        return new StoredAttachment(
                originalName,
                storedName,
                file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                file.getSize(),
                targetPath.toString()
        );
    }

    public byte[] read(String storagePath) {
        try {
            return Files.readAllBytes(Path.of(storagePath));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer archivo adjunto: " + storagePath, e);
        }
    }

    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(Path.of(storagePath));
        } catch (IOException e) {
            log.warn("No se pudo eliminar archivo temporal: {}", storagePath, e);
        }
    }

    public void deleteDirectory() {
        try {
            if (Files.exists(baseDir)) {
                try (var files = Files.list(baseDir)) {
                    files.forEach(f -> {
                        try { Files.deleteIfExists(f); } catch (IOException ignored) {}
                    });
                }
                Files.deleteIfExists(baseDir);
            }
        } catch (IOException e) {
            log.warn("No se pudo limpiar directorio de adjuntos", e);
        }
    }

    private String sanitizeFilename(String original) {
        if (original == null) return "archivo";
        return original.replaceAll("[^a-zA-Z0-9.\\-_() ]", "_").replaceAll("\\s+", "_");
    }

    public record StoredAttachment(
            String nombreOriginal,
            String nombreAlmacenado,
            String contentType,
            long sizeBytes,
            String storagePath
    ) {}
}
