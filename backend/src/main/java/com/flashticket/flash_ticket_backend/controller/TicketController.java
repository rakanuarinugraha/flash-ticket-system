package com.flashticket.flash_ticket_backend.controller;

import com.flashticket.flash_ticket_backend.dto.TicketRequest;
import com.flashticket.flash_ticket_backend.dto.TicketResponse;
import com.flashticket.flash_ticket_backend.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(){
        List<TicketResponse> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id){
        TicketResponse ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody TicketRequest request){
        TicketResponse createdTicket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }
}
