package com.example.fileupload.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import com.example.fileupload.dto.BoardForm;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
public class BoardController {
	// 입력폼
	@GetMapping("/addBoard")
		public String addBoard() {
		return "addBoard";
	}
	
	// 입력액션
	@PostMapping("/addBoard")
		public String addBoard(BoardForm boardForm) {
			log.debug(boardForm.toString());
			// 이슈 : 파일을 첨부하지 않아도 파일사이즈는 1 이다
			log.debug("MultipartFile Size: " + boardForm.getFileList().size());
			
			// 파일 분리
			List<MultipartFile> fileList = boardForm.getFileList();
			long firstFileSize = fileList.get(0).getSize();
			log.debug("firstFileSize: " + firstFileSize);
			
			// 이슈 : 파일을 첨부하지 않아도 파일사이즈는 1 이다
			if (firstFileSize > 0 ) {	// 첫번째 파일사이즈가 0이상이다 -> 첨부된 파일이 있다.
				for(MultipartFile f : fileList) {
					log.debug("파일타입: "+ f.getContentType());
					log.debug("원본이름: "+ f.getOriginalFilename());
					log.debug("파일용량: "+ f.getSize());
					// 확장자 추출 후 따로 저장
					String ext = f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf(",")+1);
					log.debug("확장자: "+ext);
					String saveName = UUID.randomUUID().toString().replace("-", "")+"." + ext;	// 파일 저장 시 중복 방지용 유니크 파일명을 만드는데 좋다.
					log.debug("저장되는파일이름: "+saveName);
					
					// 빈파일을 만들어서
					File emptyFile = new File("c:/project/upload/"+saveName); 
					// f 의 byte -> emptyFile을 복사
					try {
						f.transferTo(emptyFile);
					} catch ( Exception e) {
						log.error("파일저장실패");
						e.printStackTrace();
					}
				}
			}
			
			return "rediect:/";
		
	}
}
