package com.example.fileupload.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardRepository;
import com.example.fileupload.repository.BoardfileRepository;

import jakarta.transaction.Transactional;

@Controller
public class BoardfileController {

	@Autowired
	BoardRepository boardRepository;
	@Autowired
	private BoardfileRepository boardfileRepository;
	// 1. 삭제 확인 폼 반환
	@GetMapping("/removeBoardfile")
	public String removeBoardfile(@RequestParam(value = "fno") int fno
									, @RequestParam(value = "bno") int bno
									, Model model) {
		model.addAttribute("fno", fno);
		model.addAttribute("bno", bno);
		return "removeBoardfileConfirm";
	}	
		
	// 2. 삭제 처리
		  @PostMapping("/removeBoardfile")
		    @Transactional
		    public String removeBoardfile(@RequestParam("fno") int fno,
		                                  @RequestParam("bno") int bno,
		                                  @RequestParam("pw") String pw,
		                                  RedirectAttributes rda) {

		        Board board = boardRepository.findById(bno).orElse(null);
		        if (board == null) {
		            rda.addFlashAttribute("msg", "존재하지 않는 게시글입니다.");
		            return "redirect:/boardOne?bno=" + bno;
		        }

		        if (!board.getPw().equals(pw)) {
		            rda.addFlashAttribute("msg", "비밀번호가 일치하지 않습니다.");
		            return "redirect:/removeBoardfile?fno=" + fno + "&bno=" + bno;
		        }

		        Boardfile boardfile = boardfileRepository.findById(fno).orElse(null);
		        if (boardfile != null) {
		            File f = new File("c:/project/upload/" + boardfile.getFname() + "." + boardfile.getFext());
		            if (f.exists()) f.delete();

		            boardfileRepository.deleteById(fno);
		        }

		        return "redirect:/boardOne?bno=" + bno;
		    }
		}
