package com.dib.model;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;


@Entity
@Table(name = "status")
@Getter
@Setter
public class Status{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="status_id")
    private int id;

    @NotNull
    private String description;
    
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Lob
    private List<MultipartFile> file;
    
    private String author;
    
    private String[] fileList;
    
    private String type;

}
