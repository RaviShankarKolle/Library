package com.library.bookservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBookRequest(
        @Size(max = 32) String isbn,
        @NotBlank @Size(max = 512) String title,
        @NotBlank @Size(max = 512) String author) {
}
