package com.lms.shared.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Ref: SRS 2.13 - "thumbnails/PDFs/certificates go to object storage,
 * never the LMS server itself for video." The local-disk implementation
 * (LocalFileStorageService) is a dev-stage stand-in only - swap in an
 * S3-compatible implementation of this interface for staging/production
 * without touching any caller.
 */
public interface FileStorageService {

    /** Stores the file under the given subdirectory and returns its public URL (e.g. profile photos). */
    String store(MultipartFile file, String subdirectory);

    /**
     * Ref: SRS 8.8, 8.15, 17.24 - writes the file at exactly the given
     * reference (caller generates it, e.g. a UUID-based path, ahead of
     * time so it can be embedded in a signed upload token) somewhere never
     * covered by the public static handler; only reachable via
     * loadPrivate(), which callers must gate behind their own access check.
     */
    void writePrivate(MultipartFile file, String reference);

    /** Same as writePrivate(MultipartFile, String), for server-generated content (e.g. a rendered certificate PDF) rather than an HTTP upload. */
    void writePrivate(byte[] content, String reference);

    /** Resolves a reference previously written by writePrivate() back into a streamable Resource. */
    Resource loadPrivate(String reference);

    /** True once writePrivate() has actually landed a file at this reference. */
    boolean existsPrivate(String reference);

    long sizeOfPrivate(String reference);
}
