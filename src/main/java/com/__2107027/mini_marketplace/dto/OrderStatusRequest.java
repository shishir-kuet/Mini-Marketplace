package com.__2107027.mini_marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OrderStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(
        regexp = "processing|shipped|delivered|cancelled|completed",
        message = "Status must be one of: processing, shipped, delivered, cancelled, completed"
    )
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
