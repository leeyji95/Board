package com.imoxion.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/boardfile")
public class BoardController {

	public BoardController() {
		super();
		System.out.println("BoardController() 생성");
	}

	// FILE 게시판
	@RequestMapping(value="/file")
	public String rest() {
		System.out.println("boardfile/file 경로로...");
		return "board";
	}
}
