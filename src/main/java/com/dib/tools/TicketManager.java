package com.dib.tools;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dib.model.Ticket;
import com.dib.repository.TicketRepository;

@Service
public class TicketManager {
	
	@Autowired
	private TicketRepository ticketRepository;
	
	
	public int createNewTicket(String subject, String body, Date sentDate, String stage, String from, String fromName, boolean isName) {
		Ticket ticket = new Ticket();
		  
		  ticket.setTitle(subject);
		  ticket.setDescription(body);
		  ticket.setDateOpened(sentDate);
		  ticket.setStage(stage);
		  ticket.setTicketPusher(from);
		  
		return ticketRepository.save(ticket).getId();
	}
	
	
	
	public void reOpen(int tid) {
		Ticket ticket = ticketRepository.findById(tid);
		ticket.setStage("OPEN");
		ticketRepository.save(ticket);
	}
	
  
	
}
