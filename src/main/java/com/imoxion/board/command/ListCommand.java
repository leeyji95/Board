package com.imoxion.board.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		int pageRows = 10; // 한 ' 페이지' 에 몇 개의 글을 리스트? (디폴트 8개)
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
		String[] fileNameList = curDir.list(); 
		
		List<WriteDTO> list = new ArrayList<WriteDTO>();

		// 폴더 안에 비어있으면
		if (fileList == null) {
			message.append("[출력할 데이터(파일)가 없습니다]");
			status = "OK";
		} else { // 파일이 있는 경우
			// 파일(글) 전체 개수
			totalCnt = fileList.length;
			
			// 총 몇 페이지 분량인지
			totalPage = (int) Math.ceil(totalCnt / (double) pageRows);

			// 몇 번 글부터 ~ 몇 번 글까지
			int from = (page - 1) * pageRows;
			int end = from + pageRows;
			
//			for(int  = 0; i < end; i++) { // RNUM >= #{param1} AND RNUM < (#{param1} + #{param2})
//			}
			
			
			
			
		}
		
		request.setAttribute("status", status);
		request.setAttribute("message", message.toString());
		request.setAttribute("page", page);
		request.setAttribute("pageRows", pageRows);
		request.setAttribute("writePages", writePages);
		request.setAttribute("totalCnt", totalCnt);
		request.setAttribute("totalPage", totalPage);

	} // end execute

}
