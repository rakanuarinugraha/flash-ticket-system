package com.flashticket.flash_ticket_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private Integer totalStock;
}
