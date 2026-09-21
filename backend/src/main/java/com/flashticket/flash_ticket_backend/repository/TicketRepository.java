package com.flashticket.flash_ticket_backend.repository;

import com.flashticket.flash_ticket_backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
