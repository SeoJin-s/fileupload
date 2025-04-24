package com.example.fileupload.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;

public interface BoardRepository extends JpaRepository<Board, Integer> {
	List<BoardMapping> findAllBy(); // 원하는 컬럼만 반환하기 위해 맵핑타입을 사용
	BoardMapping findByBno(int bno);
	
	// 페이징 + 검색
	Page<BoardMapping> findByTitleContaining(String keyword, Pageable pageable);
	
	//전체 목록 페이지
	Page<BoardMapping> findAllBy(Pageable pageable);
	
}
