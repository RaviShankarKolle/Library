package com.library.bookservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCopyRequest(@NotBlank @Size(max = 64) String barcode) {
}
