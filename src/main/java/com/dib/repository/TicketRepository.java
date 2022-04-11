package com.dib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dib.model.Ticket;

@Repository("ticketRepository")
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

	Ticket findById(int id);

	List<Ticket> findByTicketPusher(String ticket_pusher);

	List<Ticket> findByTitle(String title);

}
