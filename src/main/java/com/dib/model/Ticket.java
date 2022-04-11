package com.dib.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.data.annotation.CreatedDate;

import com.sun.istack.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ticket")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private String title;

    @NotNull
    private String description;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateOpened;
    
    private Date dateClosed;
    
    private String messageId;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ticket_status", joinColumns=@JoinColumn(name="ticket_id"), inverseJoinColumns=@JoinColumn(name="status_id"))
    private List<Status> updates;

    private String stage;
    
    private String ticketPusher;

    private String[] fileList;
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="cc_ticket", joinColumns=@JoinColumn(name="ticket_id"), inverseJoinColumns=@JoinColumn(name="cc_id"))
    private List<CC> ccList;
    
        
}
