package com.imoxion.board.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imoxion.board.beans.AjaxWriteList;
import com.imoxion.board.beans.Key;
import com.imoxion.board.beans.WriteDTO;
import com.imoxion.board.command.ListCommand;

@Controller
@RequestMapping("/boardfile/*.do")
public class AjaxController {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AjaxController.class);
	public static final String DIR_PATH = "C:\\Imoxion Developer Tools\\tools\\Board\\Board\\src\\main\\webapp\\BoardDir";

	//response 할 list(data)
	
	@RequestMapping(value = "/boardfile/list.do")
	public AjaxWriteList List(HttpServletRequest request, HttpServletResponse response, Model model) {

		logger.info("--------------- list.do매핑_/board.jsp (목록 페이지)로 이동 ----------------");

		String curWorkingDir = AjaxController.DIR_PATH;
		File curDir = new File(curWorkingDir);
		logger.info("curWorking(현재디렉토리) :::  " + curWorkingDir);

		File[] fileList = curDir.listFiles();

		// 싱글톤으로 wkey 값 조종해보자
		Key keyInstance = Key.getInstance();

		if (fileList == null || fileList.length == 0) { // 폴더 비어있는 경우
			logger.info("폴더 안에 파일이 없음(X) ");

		} else { // 폴더 안에 파일이 있는 경우
			
			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isFile()) { // file 인지 확인
					logger.info("----------------------------------------------------------------------------");
					logger.info("FILE    \t" + fileList[i].getName()); // file 이름 출력

					// 파일이름
					String fileName = fileList[i].getName();

					// 글번호 
					int wkey = Integer.parseInt(fileName.substring(0, fileName.indexOf("_")));
					logger.info("글번호    \t" + wkey);

					// 제목
					int startIndex = fileName.indexOf("_");
					int endIndex = fileName.indexOf(".txt");
					String subject = fileName.substring(startIndex + 1, endIndex).trim();
					logger.info("subject\t " + subject);

					
					// 작성자, 날짜
					try (BufferedReader br = new BufferedReader(new FileReader(new File(curDir, fileName)))) {
						String line;
						StringBuffer sb = new StringBuffer();

						while ((line = (br.readLine())) != null) {
							sb.append(line + "\n");
						}

						// 작성자
						startIndex = sb.toString().indexOf("]");
						endIndex = sb.toString().indexOf("제목");
						String name = sb.toString().substring(startIndex + 2, endIndex).trim();
						logger.info("name    \t" + name);
						
						// 날짜
						int startIndex2 = sb.toString().indexOf("날짜");
						int endIndex2 = sb.toString().indexOf("작성자");
						String regDate = sb.toString().substring(startIndex2 + 4, endIndex2).trim();
						logger.info("regDate\t " + regDate);


					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			} // end for
			new ListCommand().execute(request, response);
		}
		
		return 	buildList(request, response);
	}

	public static void txt_read(File curDir, String fileName) {
	
	} // end txt_read
	

	/**
	 * 글 등록처리
	 * 
	 * @param subject
	 * @param name
	 * @param content
	 * @param request
	 * @return
	 */
	@RequestMapping("/boardfile/writeOk.do")
	@ResponseBody
	public void writeOk(@RequestParam("subject") String subject, @RequestParam("name") String name,
			@RequestParam("content") String content, HttpServletRequest request, HttpServletResponse response,
			Locale locale, Model model) {

		logger.info("----------------------------------------------------------------------------");
		logger.info("/writeOk.do 매핑 ::: ");

		// json response 에 필요한 값들
		StringBuffer message = new StringBuffer();
		String status = "FAIL"; // 기본 FAIL 설정
		int count = 0; // 데이터 개수 설정

		// 유효성 체크
		if (subject == null || subject.trim().length() == 0) {
			message.append("[유효하지 않은 parameter : 제목 필수]");
		} else if (name == null || name.trim().length() == 0) {
			message.append("[유효하지 않은 parameter : 작성자 필수]");
		} else {

			// 현재 경로 밑에 webapp 하위 폴더로 만들기...
			String path = DIR_PATH; // C:\Imoxion Developer Tools\workSpace\Board\Board\src\main\webapp\filebox
			File dirPath = new File(path); // dirPath :::: C:\WorkSpace\Board\Board\src\main\webapp\filebox
			logger.info("dirPath :::: " + dirPath);

			// 해당 디렉토리가 존재하지 않으면
			if (!dirPath.exists()) {
				// 디렉토리 생성
				if (dirPath.mkdir()) {
					logger.info("폴더 생성!!");
				} else {
					logger.info("폴더 생성 실패..");
				}
			} else { // 이미 있으면
				logger.info("이미 폴더 존재~");
			}
//-------------------------------------------------------------------------------------------------------------------------------
			// 고유한 이름으로 파일 생성하기
			File filePath = null;
			String fName = null;
			
			Key k = Key.getInstance();
			int wkey = k.getWkey();
			
			if(wkey > 0) {
				if(wkey == 1) {
					fName = wkey + "_" + subject + ".txt";
					filePath = new File(dirPath, fName); // filePath ::: C:\WorkSpace\Board\Board\src\main\webapp\filebox\fName
					logger.info("wkey == 1 일때  : " + wkey);
					
					wkey = k.createWkey(); // 1 증가시킴
					
				} else {
					fName = wkey + "_" + subject + ".txt";
					filePath = new File(dirPath, fName); // filePath ::: C:\WorkSpace\Board\Board\src\main\webapp\filebox\fName
					logger.info("wkey create() :: " + k.getWkey());
					
					wkey = k.createWkey();
				}
			}

//-------------------------------------------------------------------------------------------------------------------------------			

			// 파일이 존재하지 않으면 (wkey 값이 자동 증가되므로 파일이름 중복될 수 없음)
			if (!filePath.exists()) {
				try {
					if (filePath.createNewFile()) {
						logger.info("파일 생성!!");

						Date date = new Date();
						DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
						String formattedDate = dateFormat.format(date);

						model.addAttribute("regDate", formattedDate);

						// 파일에 쓰고 저장
						try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)))) {

							// 데이터 쓰고 저장하기
							out.println("날짜 : " + formattedDate);
							out.println("작성자 ] " + name);
							out.println("제목 : " + subject);
							out.println("내용 : " + content);
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

		request.setAttribute("status", status);
		request.setAttribute("message", message.toString());
		request.setAttribute("count", count);

		responseJSON1(request, response);
	}
	
	
	

	// response 할 메소드
	public void responseJSON1(HttpServletRequest request, HttpServletResponse response) {

		JSONObject jsonObj = new JSONObject();

		jsonObj.put("status", request.getAttribute("status"));
		jsonObj.put("message", request.getAttribute("message"));
		jsonObj.put("count", request.getAttribute("count"));
		jsonObj.put("wkey", request.getAttribute("wkey"));

		String jsonString = jsonObj.toString(); // JSON 객체 -> String 변환
		response.setContentType("application/json; charset=utf-8"); // MIME 설정

		try {
			response.getWriter().write(jsonString); // response 내보내기
		} catch (IOException e) {
			e.printStackTrace();
		}
	} // end responseJSON

	// response 할 메소드
	public AjaxWriteList buildList(HttpServletRequest request, HttpServletResponse response) {

		String curWorkingDir = AjaxController.DIR_PATH;
		File curDir = new File(curWorkingDir);

//		String[] fileList = curDir.list();
		
		WriteDTO re = new WriteDTO();
		re.
		
		AjaxWriteList result = new AjaxWriteList(); // AjaxWriteList 객체 생성해주고

		result.setStatus((String) request.getAttribute("status"));
		result.setMessage((String) request.getAttribute("message"));

		if (fileList != null) {
			result.setCount(fileList.length);
			
			// 글 목록(배열에 담기 위해 )
			JSONArray dataArr = new JSONArray(); // array
			
			for(int i = 0; i < fileList.length; i++) {
				JSONObject dataObj = new JSONObject();
				
				dataObj.put("wkey", dtoArr[i].getUid());
				dataObj.put("name", dtoArr[i].getName());
				dataObj.put("subject", dtoArr[i].getSubject());
				dataObj.put("regdate", dtoArr[i].getRegDate());
				
				// array 에 추가
				dataArr.put(dataObj);    // 배열의 경우, 값만 넣으면 되니까 
			}
		}

		// 페이징 할 때 필요한 값들
		try {
			result.setPage((Integer) request.getAttribute("page"));
			result.setTotalPage((Integer) request.getAttribute("totalPage"));
			result.setWritePages((Integer) request.getAttribute("writePages"));
			result.setPageRows((Integer) request.getAttribute("pageRows"));
			result.setTotalCnt((Integer) request.getAttribute("totalCnt"));

		} catch (Exception e) {

		}
		return result;
	} // end responseJSON
} // end AjaxController
