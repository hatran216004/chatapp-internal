package com.example.librarymanagement.dto.util;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationInfo {
    private Integer page;
    private Integer limit;
    private long totalItems;
    private Integer totalPages;
    private Boolean hasNext;
    private Boolean hasPrev;
}
