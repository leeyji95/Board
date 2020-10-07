package com.imoxion.board.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imoxion.board.beans.WriteDTO;
import com.imoxion.board.controller.AjaxController;

public class ListCommand implements Command {

	private static final Logger logger = LoggerFactory.getLogger(ListCommand.class);

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {

		logger.info("ListCommand 진입");

		// response 에 필요한 값들
		StringBuffer message = new StringBuffer();
		String status = "FAIL"; // 기본 FAIL

		// 페이징 관련 세팅값들
		int page = 1; // 현재 페이지(디폴트는 1page)
		int pageRows = 8; // 한 ' 페이지' 에 몇 개의 글을 리스트? (디폴트 8개)
		int writePages = 10; // 한 [페이징] 에 몇개의 '페이지' 를 표시? (디폴트 10)
		int totalCnt = 0; // 글은 총 몇개인지?
		int totalPage = 0; // 총 몇 '페이지' 분량인지?

		// 두개의 매개변수 받아옴
		String param;

		// page 값 : 현재 몇 페이지?
		param = request.getParameter("page");
		// 만약에 여기서 page 가 잘못 들어오거나 엉뚱한게 들어오면 -> 익셉션 처리 따로 하지 않고, page 1로 가도록 할 것.
		if (param != null && param.trim().length() != 0) {

			// 정상적으로 수행되었는지 아닌지 확인해보기 위해 try-catch 로 감싸줌
			try {
				page = Integer.parseInt(param); // 파싱하는 과정에서 null 이나 0이 나와도 -> page 1 로
			} catch (NumberFormatException e) {
				// 예외처리 하지 않음.
			}
		}

		// pageRows 값 : '한 페이지' 에 몇개의 글?
		param = request.getParameter("pageRows");
		if (param != null && param.trim().length() != 0) {
			try {
				pageRows = Integer.parseInt(param);
			} catch (NumberFormatException e) {
				// 예외처리 하지 않음.
			}
		}

		String curWorkingDir = AjaxController.DIR_PATH;
		File curDir = new File(curWorkingDir);

		File[] fileList = curDir.listFiles();
		List<WriteDTO> list = new ArrayList<WriteDTO>();

		// 폴더 안에 비어있으면
		if (fileList == null) {
			message.append("[출력할 데이터(파일)가 없습니다]");
			status = "OK";
			request.setAttribute("message", message.toString());
		} else { // 파일이 있는 경우
			// 파일(글) 전체 개수
			totalCnt = fileList.length;
			// 총 몇 페이지 분량인지
			totalPage = (int) Math.ceil(totalCnt / (double) pageRows);

			// 몇 번째 row 부터?
			int from = (page - 1) * pageRows;
			int end = from + pageRows;

			// 페이지 from 부터 end 까지 (ex. 0~10까지 for문 돌아. 10개 출력해.
			// 그럼 10개씩 출력할건데, 12개의 파일이 있어.

			for (int i = from; i < fileList.length; i++) {

				// 파일이름
				String fileName = fileList[i].getName();
				String subject = null, name = null, regdate = null;
				String content = "";

				// 글번호
				int wkey = Integer.parseInt(fileName.substring(0, fileName.indexOf("_")));

				if (wkey < end) { // 1 ~ 10, 2 ~ 20
					logger.info("wkey < end - " + wkey + " < " + end);
					// 제목
					int startIndex = fileName.indexOf("_");
					int endIndex = fileName.indexOf(".txt");
					subject = fileName.substring(startIndex + 1, endIndex).trim();

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
						name = sb.toString().substring(startIndex + 2, endIndex).trim();

						// 날짜
						int startIndex2 = sb.toString().indexOf("날짜");
						int endIndex2 = sb.toString().indexOf("작성자");
						regdate = sb.toString().substring(startIndex2 + 4, endIndex2).trim();

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					list.add(new WriteDTO(wkey, subject, name, regdate, content));
				
				} else if (wkey >= end) { // 11 ~ 20, 21 ~ 30
					logger.info("wkey >= end - " + wkey + " >= " + end);

					if (wkey < end) { // 글번호가 end 번호보다 작아지는 경우 바로 나온다.
						break;
					}
					
					// 제목
					int startIndex = fileName.indexOf("_");
					int endIndex = fileName.indexOf(".txt");
					subject = fileName.substring(startIndex + 1, endIndex).trim();

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
						name = sb.toString().substring(startIndex + 2, endIndex).trim();

						// 날짜
						int startIndex2 = sb.toString().indexOf("날짜");
						int endIndex2 = sb.toString().indexOf("작성자");
						regdate = sb.toString().substring(startIndex2 + 4, endIndex2).trim();

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				
					list.add(new WriteDTO(wkey, subject, name, regdate, content));
				}

				status = "OK";
			} // end for (from ~ end 까지의 글 출력)

			int count = fileList.length;
			request.setAttribute("count", count);

			// 글 목록(배열에 담기 위해 )
			JSONArray dataArr = new JSONArray(); // array

			for (int i = 0; i < list.size(); i++) {
				JSONObject jsonObj = new JSONObject();

				jsonObj.put("wkey", list.get(i).getWkey());
				jsonObj.put("name", list.get(i).getName());
				jsonObj.put("subject", list.get(i).getSubject());
				jsonObj.put("regdate", list.get(i).getRegdate());

				// array 에 추가
				dataArr.add(jsonObj); // 배열의 경우, 값만 넣으면 되니까
			}

			for (int i = 0; i < dataArr.size(); i++) {
				logger.info("JSONArray 에 저장된 값 : " + dataArr.get(i));
			}

			// 글목록
			request.setAttribute("data", dataArr);

		} // if-else(파일이 있는경우)

		request.setAttribute("status", status);
		request.setAttribute("message", message.toString());
		request.setAttribute("page", page);
		request.setAttribute("pageRows", pageRows);
		request.setAttribute("writePages", writePages);
		request.setAttribute("totalCnt", totalCnt);
		request.setAttribute("totalPage", totalPage);

	} // end execute

}
