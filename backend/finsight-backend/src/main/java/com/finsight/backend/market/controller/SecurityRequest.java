package com.finsight.backend.market.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SecurityRequest(
        @NotBlank(message = "symbol is required")
        @Size(max = 20, message = "symbol must be at most 20 characters")
        String symbol,
        @NotBlank(message = "companyName is required")
        String companyName,
        @Size(max = 20, message = "exchange must be at most 20 characters")
        String exchange) {
}