package com.flashticket.flash_ticket_backend.service;

import com.flashticket.flash_ticket_backend.dto.TicketRequest;
import com.flashticket.flash_ticket_backend.dto.TicketResponse;
import com.flashticket.flash_ticket_backend.entity.Ticket;
import com.flashticket.flash_ticket_backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets(){
        return ticketRepository.findAll()
                .stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id){
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket dengan ID " + id + " tidak ditemukan"));
        return TicketResponse.fromEntity(ticket);
    }

    @Transactional
    public TicketResponse createTicket(TicketRequest request){
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .totalStock(request.getTotalStock())
                .availableStock(request.getTotalStock())
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        return TicketResponse.fromEntity(savedTicket);
    }
}
