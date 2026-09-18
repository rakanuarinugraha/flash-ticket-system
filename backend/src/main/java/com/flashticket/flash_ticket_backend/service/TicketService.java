package com.flashticket.flash_ticket_backend.service;

import com.flashticket.flash_ticket_backend.dto.TicketRequest;
import com.flashticket.flash_ticket_backend.dto.TicketResponse;
import com.flashticket.flash_ticket_backend.entity.Ticket;
import com.flashticket.flash_ticket_backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "tickets", key = "'all'")
    public List<TicketResponse> getAllTickets(){
        log.info(">>> [DATABASE HIT] Mengambil semua tiket dari PostgreSQL Database...");
        return ticketRepository.findAll()
                .stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "ticket", key = "#id")
    public TicketResponse getTicketById(Long id){
        log.info(">>> [DATABASE HIT] Mengambil tiket ID: {} dari PostgreSQL Database...");
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket dengan ID " + id + " tidak ditemukan"));
        return TicketResponse.fromEntity(ticket);
    }

    @Transactional
    @CacheEvict(value = "tickets", key = "'all'")
    public TicketResponse createTicket(TicketRequest request){
        log.info(">>> Menambahkan tiket baru ke PostgreSQL & membersihkan cache 'tickets::all'");
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
