package com.dib.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dib.repository.TicketRepository;

@RestController
@RequestMapping("ticket")
public class TicketRestController {
	
	@Autowired
	private TicketRepository repository;
	
	@RequestMapping("/all")
	public ResponseEntity<Object> getTickets(){
		
		return new ResponseEntity<Object>(repository.findAll(), HttpStatus.OK);
		
	}

}
