package com.flashticket.flash_ticket_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseResponse {
    private Boolean success;
    private String message;
    private Long ticketId;
    private Integer purchasedQuantity;
    private Integer remainingStock;
    private LocalDateTime timestamp;
}
