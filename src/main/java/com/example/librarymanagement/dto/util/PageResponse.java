package com.example.librarymanagement.dto.util;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> items;
    private PaginationInfo pagination;

    public static <T> PageResponse<T> from(Page<T> page) {
        PaginationInfo pagination = PaginationInfo.builder()
                .page(page.getNumber())
                .limit(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasPrev(page.hasPrevious())
                .hasNext(page.hasNext())
                .build();

        return PageResponse.<T>builder()
                .items(page.getContent())
                .pagination(pagination)
                .build();
    }
}
