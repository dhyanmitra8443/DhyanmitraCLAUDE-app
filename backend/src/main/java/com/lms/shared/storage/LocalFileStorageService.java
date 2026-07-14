package com.lms.shared.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Dev-stage FileStorageService backed by a local directory (mounted as a
 * Docker volume - see docker-compose.yml). Not suitable for production
 * multi-instance deployments - Ref: SRS 2.13 expects real object storage.
 *
 * Split into public/ (served statically at /uploads/** - see
 * StaticResourceConfig) and private/ (never covered by that handler,
 * reachable only through loadPrivate() + a caller's own access check -
 * Ref: SRS 8.8, 8.15, 17.24) subtrees under the same base path.
 */
@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path publicBasePath;
    private final Path privateBasePath;

    public LocalFileStorageService(@Value("${app.storage.local-path}") String localPath) {
        this.publicBasePath = Path.of(localPath, "public");
        this.privateBasePath = Path.of(localPath, "private");
    }

    @Override
    public String store(MultipartFile file, String subdirectory) {
        try {
            Path dir = publicBasePath.resolve(subdirectory);
            Files.createDirectories(dir);
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + (extension != null ? "." + extension : "");
            file.transferTo(dir.resolve(filename));
            return "/uploads/" + subdirectory + "/" + filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
    }

    @Override
    public void writePrivate(MultipartFile file, String reference) {
        Path target = resolvePrivate(reference);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
    }

    @Override
    public void writePrivate(byte[] content, String reference) {
        Path target = resolvePrivate(reference);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
    }

    @Override
    public Resource loadPrivate(String reference) {
        return new FileSystemResource(resolvePrivate(reference));
    }

    @Override
    public boolean existsPrivate(String reference) {
        return Files.isRegularFile(resolvePrivate(reference));
    }

    @Override
    public long sizeOfPrivate(String reference) {
        try {
            return Files.size(resolvePrivate(reference));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file size", e);
        }
    }

    private Path resolvePrivate(String reference) {
        Path resolved = privateBasePath.resolve(reference).normalize();
        if (!resolved.startsWith(privateBasePath)) {
            throw new IllegalArgumentException("Invalid file reference.");
        }
        return resolved;
    }
}
