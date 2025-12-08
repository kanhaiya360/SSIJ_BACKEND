package com.akeshya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsResponse {
    
    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long processingOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    private Long refundedOrders;
    private Double totalRevenue;
    private Double pendingRevenue;
    private Double completedRevenue;
}