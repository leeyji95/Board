package com.imoxion.board.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.imoxion.board.beans.BoardInfo;

@Controller
@RequestMapping("/boardfile/*.do")
public class AjaxController {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AjaxController.class);
	public static final String DIR_PATH = "C:\\Imoxion Developer Tools\\tools\\Board\\Board\\src\\main\\webapp\\BoardDir";
	public static final File f = new File(DIR_PATH);

	/**
	 * 글 목록 처리
	 * 
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping(value = "/boardfile/list.do")
	@ResponseBody
	public ModelAndView List(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("page") int page, @RequestParam("pageRows") int pagerows) {

		logger.info("/list.do 매핑");
		String status = "FAIL";
		StringBuffer message = new StringBuffer();

		int writePages = 10; // 한 [페이징] 에 몇개의 '페이지' 를 표시? (디폴트 10)
		int totalCnt = 0; // 글은 총 몇개인지?
		int totalPage = 0; // 총 몇 '페이지' 분량인지?

		String[] fNnameList = f.list();
		BoardInfo BI = new BoardInfo();
		List<BoardInfo> list = new ArrayList<BoardInfo>();
		List<Integer> keySort = BI.Sort(); // 정렬된 키값 저장된 리스트

		// 폴더 안에 비어있으면
		if (fNnameList.length == 0) {
			message.append("[출력할 데이터(파일)가 없습니다]");
		} else { // 파일이 있는 경우

			// 파일(글) 전체 개수
			totalCnt = fNnameList.length;

			// 총 몇 페이지 분량인지
			totalPage = (int) Math.ceil(totalCnt / (double) pagerows);

			// 몇 번 글부터 ~ 몇 번 글까지
			int from = (page - 1) * pagerows;
			int end = from + pagerows;

			list = BI.listFile(from, end);
			status = "OK";
		}

		ModelAndView mv = new ModelAndView("jsonView");

		mv.addObject("status", status);
		mv.addObject("message", message.toString());
		mv.addObject("page", page);
		mv.addObject("pagerows", pagerows);
		mv.addObject("totalcnt", totalCnt);
		mv.addObject("totalpage", totalPage);
		mv.addObject("writepages", writePages);
		mv.addObject("data", list);

		mv.setViewName("jsonView");

		return mv;
	}

	/**
	 * 글 등록 처리
	 * 
	 * @param subject
	 * @param name
	 * @param content
	 * @param request
	 * @return
	 */
	@RequestMapping("/boardfile/writeOk.do")
	@ResponseBody
	public ModelAndView writeOk(@RequestParam("subject") String subject, @RequestParam("name") String name,
			@RequestParam("content") String content, HttpServletRequest request, HttpServletResponse response,
			Locale locale, Model model) {

		logger.info("/writeOk.do 매핑 ::: ");
		String status = "FAIL";
		StringBuffer message = new StringBuffer();
		int count = 0;

		// 유효성 체크
		if (subject == null || subject.trim().length() == 0) {
			message.append("[유효하지 않은 parameter : 제목 필수]");
		} else if (name == null || name.trim().length() == 0) {
			message.append("[유효하지 않은 parameter : 작성자 필수]");
		} else {

			// 해당 디렉토리가 존재하지 않으면
			if (!f.exists()) {
				// 디렉토리 생성
				if (f.mkdir()) {
					logger.info("폴더 생성!!");
				} else {
					logger.info("폴더 생성 실패..");
				}
			} else { // 이미 있으면
				logger.info("이미 폴더 존재~");
			}

			File filePath = null;
			String fName = null;

			// 키값 생성
			AtomicInteger fileKey = new AtomicInteger();
			BoardInfo BI = new BoardInfo();
			int wkey = BI.lastWkey();

			if (wkey == 0) {
				fileKey.incrementAndGet(); // 1 증가하고 얻기
				logger.info("fileKey(1이 나와야함) - " + fileKey.get());
			} else {
				fileKey.set(wkey); // 글의 마지막 번호로 세팅해주고
				fileKey.incrementAndGet(); // 1증가
				logger.info("fileKey(1이 아닌경우) - " + fileKey.get());
			}

			// 파일 생성
			fName = fileKey.get() + "_" + subject + ".txt";
			filePath = new File(f, fName);

			// 파일이 존재하지 않으면 (wkey 값이 자동 증가되므로 파일이름 중복될 수 없음)
			if (!filePath.exists()) {
				try {
					if (filePath.createNewFile()) {
						logger.info("파일 생성!!");

						Date date = new Date();
						DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
						String formattedDate = dateFormat.format(date);

						// 파일에 쓰고 저장
						try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)))) {

							// 데이터 쓰고 저장하기
							out.println("날짜 : " + formattedDate);
							out.println("작성자 ] " + name);
							out.println("제목: " + subject);
							out.println("내용: " + content);
							out.flush();
							logger.info("subject : " + subject + ", name : " + name + ", content : " + content
									+ " 날짜 : " + formattedDate);
							count = 1;
							message.append(count + "개" + " 글 등록(저장) 성공!");
							status = "OK";

						} catch (IOException e) {
							message.append("파일 저장 실패 :: " + e.getMessage());
							logger.error("Failed saving file : " + e.getMessage());
						}
					} else {
						logger.error("파일 생성 실패....");
					}
				} catch (IOException e) {
					e.getMessage();
				}
			} else {
				logger.error("파일이 이미 존재합니다..."); // 파일이 이미 있다면(wkey 값이 증가되지 않은 것)
			}
		} // end if

		ModelAndView mv = new ModelAndView("jsonView");

		mv.addObject("status", status);
		mv.addObject("message", message.toString());
		mv.addObject("count", count);
		mv.setViewName("jsonView");

		return mv;
	}

	/**
	 * 글 조회 처리
	 * 
	 * @param request
	 * @param response
	 * @param wkey
	 */
	@RequestMapping("/boardfile/view.do")
	@ResponseBody
	public ModelAndView View(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("wkey") String wkey) {
		logger.info(" view.do 진입");

		StringBuffer message = new StringBuffer();
		String status = "FAIL"; // 기본 FAIL

		BoardInfo BI = new BoardInfo();
		List<BoardInfo> list = new ArrayList<BoardInfo>();

		list = BI.ViewFile(wkey);

		if (list.size() != 0) {
			status = "OK";
			message.append("");
		} else {
			message.append("글 조회 실패하였습니다.");
		}

		ModelAndView mv = new ModelAndView();

		mv.addObject("status", status);
		mv.addObject("message", message.toString());
		mv.addObject("data", list);
		
		
		mv.setViewName("jsonView");

		return mv;

	} // end View()

	/**
	 * 글 수정 처리
	 * 
	 * @param request
	 * @param response
	 * @param wkey
	 * @param subject
	 * @param content
	 * @throws Exception 
	 */
	@RequestMapping("/boardfile/updateOk.do")
	@ResponseBody
	public ModelAndView UpdateOk(HttpServletRequest request, HttpServletResponse response, @RequestParam("wkey") String wkey,
			@RequestParam("subject") String subject, @RequestParam("content") String content) throws Exception {

		logger.info("UpdateOk.do 진입");

		// response 에 필요한 값들
		StringBuffer message = new StringBuffer();
		String status = "FAIL"; // 기본 FAIL
		int count = 0;

		BoardInfo BI = new BoardInfo();
		List<BoardInfo> list = new ArrayList<BoardInfo>();
		
		count = BI.UpdateFile(subject, content, wkey);
		
		logger.info("count - " + count );
		
		if(count == 1) {
			status = "OK";
		} else {
			message.append("수정 실패하였습니다.");
		}
		
		ModelAndView mv = new ModelAndView();

		mv.addObject("status", status);
		mv.addObject("message", message.toString());
		mv.addObject("count", count);
		mv.setViewName("jsonView");

		return mv;
		
	} // end UpdateOk

} // end AjaxController
