package com.example.fileupload.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "board")
public class Board {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int bno;
	@Column(name = "title")
	private String title;
	@Column(name = "pw")
	private String pw;
	
	// 사용하지 않는 연관 관계 설정은 주석처리
	// Boardfile 관계설정 X
	// @OneToMany(mappedBy = "board")
	// private List<Boardfile> boardfiles = new ArrayList<>();
}