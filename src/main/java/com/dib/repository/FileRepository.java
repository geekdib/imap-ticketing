package com.dib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dib.model.FileModel;

@Repository
@Transactional
public interface FileRepository extends JpaRepository<FileModel, Integer> {
	
	List<FileModel> findByStatusId(int statusId);
	
	FileModel findById(int id);

}
