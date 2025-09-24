package com.english.api.common.dto;

/**
 * Created by hungpham on 9/22/2025
 */
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record PaginationResponse(Meta meta, Object result) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public static PaginationResponse from(Page<?> page, Pageable pageable) {
        Meta meta = new Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
        return new PaginationResponse(meta, page.getContent());
    }
    public record Meta(int page, int pageSize, int pages, long total) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}
