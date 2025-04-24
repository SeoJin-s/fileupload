package com.example.fileupload.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fileupload.dto.BoardForm;
import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;
import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardRepository;
import com.example.fileupload.repository.BoardfileRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class BoardController {

    private final BoardfileController boardfileController;
   @Autowired BoardRepository boardRepository;
   @Autowired BoardfileRepository boardfileRepository;

    BoardController(BoardfileController boardfileController) {
        this.boardfileController = boardfileController;
    }
   
   @GetMapping("/boardOne")
   public String boardOne(Model model, @RequestParam(value = "bno") int bno) {
       BoardMapping boardMapping = boardRepository.findByBno(bno);
       
       List<Boardfile> fileList = boardfileRepository.findByBno(bno);

       // 각 파일마다 imgPath를 만들어서 리스트에 다시 담기
       List<Map<String, Object>> fileMapList = new ArrayList<>();
       for (Boardfile file : fileList) {
           Map<String, Object> map = new HashMap<>();
           map.put("fno", file.getFno());
           map.put("fname", file.getFname());
           map.put("fext", file.getFext());
           map.put("fsize", file.getFsize());
           map.put("foriginname", file.getForiginname());
           map.put("imgPath", file.getFname() + "." + file.getFext()); // 🔥 핵심
           // ✅ 이미지인지 여부 판단 (Mustache에서 {{#isImage}} 처리용)
           String ext = file.getFext().toLowerCase();
           boolean isImage = ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("gif") || ext.equals("webp");
           map.put("isImage", isImage);
           
           fileMapList.add(map);
       }

       model.addAttribute("boardMapping", boardMapping);
       model.addAttribute("fileList", fileMapList); // fileList → fileMapList로 대체
       return "boardOne";
   }

   
   @GetMapping({"/", "/boardList"})
   public String boardList(Model model,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "") String search) {

       int size = 5;
       Pageable pageable = PageRequest.of(page - 1, size, Sort.by("bno").descending());

       Page<BoardMapping> boards;
       if (search.isEmpty()) {
           boards = boardRepository.findAllBy(pageable);
       } else {
           boards = boardRepository.findByTitleContaining(search, pageable);
       }

       List<Map<String, Object>> list = new ArrayList<>();
       for (BoardMapping b : boards.getContent()) {
           Map<String, Object> map = new HashMap<>();
           map.put("bno", b.getBno());
           map.put("title", b.getTitle());

           List<Boardfile> files = boardfileRepository.findByBno(b.getBno());
           if (files != null && !files.isEmpty()) {
               Boardfile file = files.get(0);
               String imgPath = file.getFname() + "." + file.getFext();
               map.put("imgPath", imgPath);
           }

           list.add(map);
       }

       List<Map<String, Object>> pages = new ArrayList<>();
       for (int i = 1; i <= boards.getTotalPages(); i++) {
           Map<String, Object> p = new HashMap<>();
           p.put("pageNum", i);
           p.put("isCurrent", i == page);
           pages.add(p);
       }

       model.addAttribute("list", list);
       model.addAttribute("eachPage", pages);
       model.addAttribute("currentPage", page);
       model.addAttribute("totalPages", boards.getTotalPages());
       model.addAttribute("search", search);

       return "boardList";
   }

   
   // 입력폼
   @GetMapping("/addBoard")
   public String addBoard() {
      return "addBoard";
   }
   
   // 입력액션
   @PostMapping("/addBoard")
   @Transactional
   public String addBoard(BoardForm boardForm) {
      log.debug(boardForm.toString());
      // ISSUE : 파일을 첨부하지 않아도 fileSize는 1이다.
      log.debug("MultipartFile Size: " + boardForm.getFileList().size());
      
      Board board = new Board();
      board.setTitle(boardForm.getTitle());
      board.setPw(boardForm.getPw());
      boardRepository.save(board); // board 저장
      int bno = board.getBno(); // board insert 후 bno 변경되었는지 확인
      log.debug("bno: " + bno);
      
      // 파일 분리
      List<MultipartFile> fileList = boardForm.getFileList();
      long firstFileSize = fileList.get(0).getSize();
      log.debug("firstFileSize: "  + firstFileSize);
      
      // ISSUE : 파일을 첨부하지 않아도 fileSize는 1이다.
      if(firstFileSize > 0) { // 첫 번째 파일 사이즈가 0 이상이다 -> 첨부된 파일이 있다.
         // 파일 유효성 검사 코드
    	  for(MultipartFile f : fileList) {
        	 if(f.getContentType().equals("application/octet-stream") || f.getSize() > 10*1024*1024) { // 1kbyte = 1024
        		 return "redirect:/addBoard"; // msg추가
        	 }
         }	 
        	// 파일 업로드 진행코드 
        for(MultipartFile f : fileList) {	 
            log.debug("파일 타입: " + f.getContentType());
            log.debug("파일 이름: " + f.getName());
            log.debug("원본 이름: " + f.getOriginalFilename());
            log.debug("파일 크기: " + f.getSize());
            // 확장자만 추출
            String ext = f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf(".") + 1);
            log.debug("확장자: " + ext);
            String saveName = UUID.randomUUID().toString().replace("-", "");
            log.debug("저장 파일 이름: " + saveName);
            
            File emptyFile = new File("c:/project/upload/" + saveName + "." + ext);
            // f의 byte -> emptyFile 복사
            try {
               f.transferTo(emptyFile);
            } catch (Exception e) {
               log.error("파일 저장 실패");
               e.printStackTrace();
            }
            
            // boardfile 테이블에도 파일 정보 저장
            Boardfile boardfile = new Boardfile();
            boardfile.setBno(board.getBno());
            boardfile.setFext(ext);
            boardfile.setFname(saveName);
            boardfile.setForiginname(f.getOriginalFilename());
            boardfile.setFsize(f.getSize());
            boardfile.setFtype(f.getContentType());
            boardfileRepository.save(boardfile);
         }
      }
      return "redirect:/boardList";
   }

   //삭제
   @Transactional
   @GetMapping("/removeBoard")
   public String removeBoard(@RequestParam("bno") int bno) {
	   // 1. 첨부파일 먼저 삭제
	   boardfileRepository.deleteByBno(bno);
	   
	   // 2. 게시글 삭제
	   boardRepository.deleteById(bno);
	   
	   return "redirect:/boardList";   
   }
   
   // 삭제 확인 비밀번호
   @GetMapping("/removeBoardConfirm")
   public String deleteConfirmForm(@RequestParam("bno") int bno, Model model) {
	   model.addAttribute("bno", bno);
	return "deleteConfirm";
	   
   }
   
   // 삭제 처리
   @PostMapping("/removeBoard")
   @Transactional
   public String deleteBoard(@RequestParam("bno") int bno
		   					, @RequestParam("pw") String pw
		   					, RedirectAttributes rda) {
	   
	   Board board = boardRepository.findById(bno).orElse(null);
	   if (board == null) {
		   rda.addFlashAttribute("msg", "존재하지 않는 게시글입니다.");
		   return "redirect:/boardList";
	   }
	   
	   if (!board.getPw().equals(pw)) {
		   rda.addFlashAttribute("msg", "비밀번호가 일치하지 않습니다.");
		   return "redirect:/removeBoard?bno=" +bno;
	   }
   
	    boardfileRepository.deleteByBno(bno); // 파일 먼저 삭제
	    boardRepository.deleteById(bno);      // 게시글 삭제

	    return "redirect:/boardList";
	}
  
   // 수정
   @GetMapping("/modifyBoard")
   public String modifyBoardForm(@RequestParam("bno") int bno, Model model) {
	   Board board = boardRepository.findById(bno).orElse(null);
	   	if (board == null) return "redirect:/boardList";
	   	
	   	List<Boardfile> files = boardfileRepository.findByBno(bno);
	   	model.addAttribute("board", board);
	   	model.addAttribute("fileList", files);
	   	return "modifyBoard";
   }

   // 수정 파일
   @PostMapping("/modifyBoard")
   @Transactional
   public String modifyBoard(@RequestParam("bno") int bno
					          , @RequestParam("pw") String pw
					          , @RequestParam("title") String title
					          , @RequestParam(value = "file", required = false) MultipartFile file
					          , RedirectAttributes rda) { 
	   Board board = boardRepository.findById(bno).orElse(null);
	   if (board == null || !board.getPw().equals(pw)) {
		   rda.addFlashAttribute("msg", "비밀번호가 일치하지 않습니다.");
		   return "redirect:/modifyBoard?bno=" +bno;
	   }
	   board.setTitle(title);
	   boardRepository.save(board);
	   
	  // 기존 파일 삭제 후 새파일 등록 ( 선택가능 )
	   if (file != null && !file.isEmpty()) {
		   boardfileRepository.deleteByBno(bno); // 기존 파일 모두 삭제
	   
	  // 새 파일 저장
		  String originName = file.getOriginalFilename();
		  String ext = originName.substring(originName.lastIndexOf(".") + 1);
		  String uuid = UUID.randomUUID().toString();
		  File f = new File("c:/project/upload/" + uuid + "." + ext);
		  try {
			  file.transferTo(f);
		  }catch(Exception e) {
			  e.printStackTrace();
		  }
		  
		  Boardfile bf = new Boardfile();
		  bf.setBno(bno);
		  bf.setFname(uuid);
		  bf.setFext(ext);
		  bf.setFsize(file.getSize());
		  bf.setForiginname(originName);
		  bf.setFtype(file.getContentType()); // ✅ MIME 타입 설정!
		  boardfileRepository.save(bf);
	   }
	   
	   return "redirect:/boardOne?bno=" + bno;
   }

}