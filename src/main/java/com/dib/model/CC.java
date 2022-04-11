package com.dib.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cc")
public class CC {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="cc_id")
	private int ccId;
	
    private int ticketId;
    
    private String ccEmail;

}
