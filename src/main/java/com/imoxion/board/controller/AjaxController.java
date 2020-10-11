package com.imoxion.board.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.imoxion.board.beans.BoardInfo;
import com.imoxion.board.beans.WriteDTO;
import com.imoxion.board.command.ListCommand;

@Controller
@RequestMapping("/boardfile/*.do")
public class AjaxController {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AjaxController.class);
	public static final String DIR_PATH = "C:\\Imoxion Developer Tools\\tools\\Board\\Board\\src\\main\\webapp\\BoardDir";

	private File f = new File(DIR_PATH);
	private String status = "FAIL";
	private StringBuffer message = new StringBuffer();
	int count = 0;
	
	/**
	 * 글 목록 처리
	 * 
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping(value = "/boardfile/list.do")
	@ResponseBody
	public void List(HttpServletRequest request, HttpServletResponse response, Model model) {

		logger.info("list.do 매핑");

		String curWorkingDir = AjaxController.DIR_PATH;
		File curDir = new File(curWorkingDir);
		logger.info("curWorking(현재디렉토리) - " + curWorkingDir);

		File[] fileList = curDir.listFiles();

		// 페이징처리 위한 코드 실행
		new ListCommand().execute(request, response);

		JSONObject jsonObj = new JSONObject();

		if (fileList == null || fileList.length == 0) { // 폴더 비어있는 경우
			logger.info("폴더 안에 파일이 없음(X) ");
		} else {

			// 폴더 안에 파일이 있는 경우

			jsonObj.put("status", request.getAttribute("status"));
			jsonObj.put("message", request.getAttribute("message"));
			jsonObj.put("count", request.getAttribute("count"));
			jsonObj.put("wkey", request.getAttribute("wkey"));
			jsonObj.put("page", request.getAttribute("page"));
			jsonObj.put("pagerows", request.getAttribute("pageRows"));
			jsonObj.put("writepages", request.getAttribute("writePages"));
			jsonObj.put("totalcnt", request.getAttribute("totalCnt"));
			jsonObj.put("totalpage", request.getAttribute("totalPage"));
			jsonObj.put("data", request.getAttribute("data"));

		}
		String jsonString = jsonObj.toString(); // JSON 객체 -> String 변환
		response.setContentType("application/json; charset=utf-8"); // MIME 설정

		try {
			response.getWriter().write(jsonString); // response 내보내기
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		logger.info("----------------------------------------------------------------------------");
		logger.info("/writeOk.do 매핑 ::: ");

		// json response 에 필요한 값들
//		StringBuffer message = new StringBuffer();
//		String status = "FAIL"; // 기본 FAIL 설정
//		int count = 0; // 데이터 개수 설정

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

		mv.setViewName("jsonView");
		mv.addObject("status", status);
		mv.addObject("message", message.toString());
		mv.addObject("count", count);
		
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
	public void View(HttpServletRequest request, HttpServletResponse response, @RequestParam("wkey") String wkey) {
		logger.info(" view.do 진입");

		// response 에 필요한 값들
		StringBuffer message = new StringBuffer();
		String status = "FAIL"; // 기본 FAIL

		// 해당 wkey 글번호로 제목, 작성자, 내용
		File curDir = new File(AjaxController.DIR_PATH);
		List<WriteDTO> list = new ArrayList<WriteDTO>();

		FilenameFilter filter = new FilenameFilter() {

			public boolean accept(File curDir, String name) {
				return name.startsWith(wkey);
			}
		};

		String[] fileName = curDir.list(filter); // 해당 글번호로 시작하는 파일이름을 배열로 출력한다.

		String subject = null, name = null, content = null;
		String regdate = "";

		for (int i = 0; i < fileName.length; i++) {
			logger.info(fileName[i]);

			// 제목
			int startIndex = fileName[i].indexOf("_");
			int endIndex = fileName[i].indexOf(".txt");
			subject = fileName[i].substring(startIndex + 1, endIndex).trim();

			// 작성자, 내용
			try (BufferedReader br = new BufferedReader(
					new FileReader(new File(AjaxController.DIR_PATH, fileName[i])))) {
				String line;
				StringBuffer sb = new StringBuffer();

				while ((line = (br.readLine())) != null) {
					sb.append(line + "\n");
				}

				// 작성자
				startIndex = sb.toString().indexOf("]");
				endIndex = sb.toString().indexOf("제목");
				name = sb.toString().substring(startIndex + 2, endIndex).trim();

				// 내용
				int startIndex2 = sb.toString().indexOf("내용");
				content = sb.toString().substring(startIndex2 + 3).trim();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			list.add(new WriteDTO(Integer.parseInt(wkey), subject, name, regdate, content));
		}

		JSONArray dataArr = new JSONArray(); // array
		JSONObject jsonObj = new JSONObject();

		for (int i = 0; i < list.size(); i++) {

			jsonObj.put("wkey", list.get(i).getWkey());
			jsonObj.put("name", list.get(i).getName());
			jsonObj.put("subject", list.get(i).getSubject());
			jsonObj.put("content", list.get(i).getContent());
			jsonObj.put("status", "OK");
			jsonObj.put("message", "글을 조회하는데 실패하였습니다.");

			// array 에 추가
			dataArr.add(jsonObj);
		}

		for (int i = 0; i < dataArr.size(); i++) {
			logger.info("JSONArray 에 저장된 값 : " + dataArr.get(i));
		}

		// 오브젝트와 배열에 값을 넣을 때 모두 put 사용한다.
		String jsonString = jsonObj.toString(); // JSON 객체가 --> String 변환
		response.setContentType("application/json; charset=utf-8"); // MIME 설정

		try {
			response.getWriter().write(jsonString); // response 내보내기
		} catch (IOException e) {
			e.printStackTrace();
		}

	} // end View()

	/**
	 * 글 수정 처리
	 * 
	 * @param request
	 * @param response
	 * @param wkey
	 * @param subject
	 * @param content
	 * @throws IOException
	 */
	@RequestMapping("/boardfile/updateOk.do")
	@ResponseBody
	public void UpdateOk(HttpServletRequest request, HttpServletResponse response, @RequestParam("wkey") String wkey,
			@RequestParam("subject") String subject, @RequestParam("content") String content) throws IOException {

		logger.info("UpdateOk.do 진입");

		// response 에 필요한 값들
		StringBuffer message = new StringBuffer();
		String status = "FAIL"; // 기본 FAIL
		int count = 0;

		// 해당 wkey 글번호로 제목, 작성자, 내용
		File curDir = new File(AjaxController.DIR_PATH);
		List<WriteDTO> list = new ArrayList<WriteDTO>();

		// 특정 파일 가져오기
		FilenameFilter filter = new FilenameFilter() {

			public boolean accept(File curDir, String name) {
				return name.startsWith(wkey);
			}
		};

		String[] fileName = curDir.list(filter); // 해당 글번호로 시작하는 파일이름을 배열로 출력한다.

		// 어차피 1개 밖에 없으므로, 0번째 fileName 가져오기
		logger.info("해당 파일이름 - " + fileName[0]);

		int startIndex = 0, endIndex = 0;
		String name = null;
		String fName = null;

		// 해당 파일을 읽어들여서 제목 , 내용 덮어쓰기 (수정)
		// 해당 파일에서 작성자만 뽑고, 삭제 후 같은 글번호로 파일 생성한다.
		try (BufferedReader br = new BufferedReader(new FileReader(new File(AjaxController.DIR_PATH, fileName[0])))) {
			String line;
			StringBuffer sb = new StringBuffer();

			while ((line = (br.readLine())) != null) {
				sb.append(line + "\n");
			}

			// 작성자
			startIndex = sb.toString().indexOf("]");
			endIndex = sb.toString().indexOf("제목");
			name = sb.toString().substring(startIndex + 2, endIndex).trim();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// 해당 파일 삭제
		File[] f = curDir.listFiles(filter);
		if (f[0].exists()) { // 만약 해당 파일 존재하면,
			f[0].delete(); // 삭제

			// 파일 이름 다시 생성
			fName = wkey + "_" + subject + ".txt";
			File updateFile = new File(AjaxController.DIR_PATH, fName); // 해당 경로에 파일 객체 생성

			if (!updateFile.exists()) {
				if (updateFile.createNewFile()) {

				}

				Date date = new Date();
				DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
				String formattedDate = dateFormat.format(date);

				// 파일에 쓰고 저장
				try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(updateFile)))) {

					// 데이터 쓰고 저장하기
					out.println("날짜 : " + formattedDate);
					out.println("작성자 ] " + name);
					out.println("제목: " + subject);
					out.println("내용: " + content);
					out.flush();
					logger.info("수정된 subject : " + subject + ", name : " + name + ", content : " + content + " 날짜 : "
							+ formattedDate);
					count = 1;
					message.append(count + "개" + " 글 수정 성공!");
					status = "OK";

					list.add(new WriteDTO(Integer.parseInt(wkey), subject, name, formattedDate, content));

				} catch (IOException e) {
//					message.append("파일 저장 실패 :: " + e.getMessage());
					message.append("글 수정 실패...");
					logger.error("Failed saving file : " + e.getMessage());
				}

			} else {

			}

		}

		JSONArray dataArr = new JSONArray(); // array
		JSONObject jsonObj = new JSONObject();

		for (int i = 0; i < list.size(); i++) {

			jsonObj.put("status", "OK");
			jsonObj.put("count", count);
			jsonObj.put("wkey", list.get(i).getWkey());
			jsonObj.put("name", list.get(i).getName());
			jsonObj.put("subject", list.get(i).getSubject());
			jsonObj.put("content", list.get(i).getContent());
			jsonObj.put("message", message.toString());

			// array 에 추가
			dataArr.add(jsonObj);
		}

		for (int i = 0; i < dataArr.size(); i++) {
			logger.info("JSONArray 에 저장된 값 : " + dataArr.get(i));
		}

		// 오브젝트와 배열에 값을 넣을 때 모두 put 사용한다.
		String jsonString = jsonObj.toString(); // JSON 객체가 --> String 변환
		response.setContentType("application/json; charset=utf-8"); // MIME 설정

		try {
			response.getWriter().write(jsonString); // response 내보내기
		} catch (IOException e) {
			e.printStackTrace();
		}

	} // end UpdateOk

} // end AjaxController
