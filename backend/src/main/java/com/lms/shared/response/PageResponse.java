package com.lms.shared.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Matches openapi.yaml's {content, page} pagination shape used across every chapter's list endpoints. */
public record PageResponse<T>(List<T> content, PageMeta page) {

    public static <E, T> PageResponse<T> from(Page<E> springPage, Function<E, T> mapper) {
        return new PageResponse<>(
                springPage.getContent().stream().map(mapper).toList(),
                new PageMeta(springPage.getNumber(), springPage.getSize(), springPage.getTotalElements(), springPage.getTotalPages())
        );
    }
}
