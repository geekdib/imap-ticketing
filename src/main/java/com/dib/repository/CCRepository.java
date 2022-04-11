package com.dib.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dib.model.CC;


@Repository
@Transactional
public interface CCRepository extends JpaRepository<CC, Integer> {
	
	CC findById(int id);

}
