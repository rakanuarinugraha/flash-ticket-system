package com.flashticket.flash_ticket_backend.service;

import com.flashticket.flash_ticket_backend.dto.PurchaseRequest;
import com.flashticket.flash_ticket_backend.dto.PurchaseResponse;
import com.flashticket.flash_ticket_backend.dto.TicketRequest;
import com.flashticket.flash_ticket_backend.dto.TicketResponse;
import com.flashticket.flash_ticket_backend.entity.Ticket;
import com.flashticket.flash_ticket_backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String STOCK_KEY_PREFIX = "ticket:stock:";

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

        String stockKey = STOCK_KEY_PREFIX + savedTicket.getId();
        redisTemplate.opsForValue().set(stockKey, String.valueOf(savedTicket.getAvailableStock()));
        log.info(">>> Kuota Tiket ID: {} diinisialisasikan di Redis: {}", savedTicket.getId(), savedTicket.getAvailableStock());

        return TicketResponse.fromEntity(savedTicket);
    }

    @Transactional
    @CacheEvict(value = "ticket", key = "#request.ticketId")
    public PurchaseResponse purchaseTicket(PurchaseRequest request){
        Long ticketId = request.getTicketId();
        int quantity = (request.getQuantity() != null && request.getQuantity() > 0) ? request.getQuantity() : 1;
        String stockKey = STOCK_KEY_PREFIX + ticketId;

        if(Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))){
            Ticket ticketFromDb = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Tiket tidak ditemukan"));
            redisTemplate.opsForValue().set(stockKey, String.valueOf(ticketFromDb.getAvailableStock()));
        }

        Long remainingStock = redisTemplate.opsForValue().decrement(stockKey, quantity);

        if(remainingStock != null && remainingStock < 0){
            redisTemplate.opsForValue().increment(stockKey, quantity);
            throw new RuntimeException("Ticket SOLD OUT!");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Tiket tidak ditemukan"));
        ticket.setAvailableStock(ticket.getAvailableStock() - quantity);
        ticketRepository.save(ticket);

        log.info(">>> [FLASH SALE SUCCESS] Tiket ID: {} berhasil dibeli sebanyak {}. Sisa stok: {}", ticketId, quantity, remainingStock);

        return PurchaseResponse.builder()
                .success(true)
                .message("Tiket berhasil dibeli")
                .ticketId(ticketId)
                .purchasedQuantity(quantity)
                .remainingStock(remainingStock != null ? remainingStock.intValue() : 0)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
