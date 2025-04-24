package com.example.fileupload.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.Boardfile;

public interface BoardfileRepository extends JpaRepository<Boardfile, Integer> {
	List<Boardfile> findByBno(int bno);

	// PK 한행 삭제
	// void deleteById(int key) 사용
	
	// FK 여러행 삭제 ( board 삭제 시 같이 삭제 : 트랜젝션)
	void deleteByBno(int bno);
	
	
}