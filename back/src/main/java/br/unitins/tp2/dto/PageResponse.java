package br.unitins.tp2.dto;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages) {

    public static <T, R> PageResponse<R> of(
            List<T> items,
            int page,
            int pageSize,
            long totalItems,
            Function<T, R> mapper) {
        int totalPages = pageSize <= 0 ? 0 : (int) Math.ceil((double) totalItems / pageSize);
        return new PageResponse<>(items.stream().map(mapper).toList(), page, pageSize, totalItems, totalPages);
    }
}
