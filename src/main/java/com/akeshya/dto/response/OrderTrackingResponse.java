package com.akeshya.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class OrderTrackingResponse {
    private Long id;
    private String status;
    private String description;
    private LocalDateTime createdAt;
}