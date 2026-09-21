package com.flashticket.flash_ticket_backend.dto;

import com.flashticket.flash_ticket_backend.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer totalStock;
    private Integer availableStock;
    private LocalDateTime createdAt;

    public static TicketResponse fromEntity(Ticket ticket){
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .price(ticket.getPrice())
                .totalStock(ticket.getTotalStock())
                .availableStock(ticket.getAvailableStock())
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}
