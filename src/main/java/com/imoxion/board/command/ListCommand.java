package com.imoxion.board.command;

import java.io.File;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.imoxion.board.beans.Key;
import com.imoxion.board.controller.AjaxController;

public class ListCommand implements Command {
	
	@Override
	
	public void execute(HttpServletRequest request, HttpServletResponse response) {

		System.out.println("ListCommand 진입");
		
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
		status = "OK";

		// 폴더 안에 비어있으면
		if (fileList == null) {
			message.append("[리스트할 데이터(파일)가 없습니다]");
		} else { // 파일이 있는 경우
			// 파일(글) 전체 개수
			totalCnt = fileList.length;

			// 총 몇 페이지 분량인지
			totalPage = (int) Math.ceil(totalCnt / (double) pageRows);

			// 몇 번째 row 부터?
			int from = (page - 1) * pageRows + 1;
			
			System.out.println("from :: " + from);
			// 페이지 from 부터 from + pageRows 까지

			// 1. 글번호 wkey 값을 뽑는다.
			// 2. 페이지 번호 RNUM 붙인다.

//			int RNUM = 0; // 디폴트 0 세팅
//			String fileName = null;
//			for (int i = 0; i < fileList.length; i++) {
//				if (fileList[i].isFile()) { // file 인지 확인
//					// 파일이름
//					fileName = fileList[i].getName();
//				}
//			} // end for
			System.out.println("Key.getInstance().getWkey() ::: 글번호 :::  "  + Key.getInstance().getWkey());
			if(Key.getInstance().getWkey() == fileList.length) {
				int wkey = Key.getInstance().getWkey(); // 마지막 키값..
				for(int i = 0; i < wkey; i++) {
					// 파일이름
					String fileName = fileList[i].getName();

					// 제목
					int startIndex = fileName.indexOf("_");
					int endIndex = fileName.indexOf(".txt");
					String subject = fileName.substring(startIndex + 1, endIndex).trim();
					
				}
				
			}

//					for (int j = 1; j <= wkey; j++) {
//
//						// 글번호
//						int wkey = Integer.parseInt(fileName.substring(0, fileName.indexOf("_")));
//						// 제목
//						int startIndex = fileName.indexOf("_");
//						int endIndex = fileName.indexOf(".txt");
//						String subject = fileName.substring(startIndex + 1, endIndex).trim();
//						System.out.println("wkey ::: " + wkey);
////					}
//			for (int i = from - 1; i < (from + pageRows) - 1; i++) {
//				// 파일이름
//				String fileName = fileList[i].getName();
//
//				// 글번호
//				int wkey = Integer.parseInt(fileName.substring(0, fileName.indexOf("_")));
//				// 제목
//				int startIndex = fileName.indexOf("_");
//				int endIndex = fileName.indexOf(".txt");
//				String subject = fileName.substring(startIndex + 1, endIndex).trim();
//				System.out.println("wkey ::: " + wkey);
//			}
			
			
			
			status = "OK";
		}

		System.out.println(status);
		request.setAttribute("status", status);
		request.setAttribute("message", message.toString());
		request.setAttribute("list", Arrays.asList(fileList));
		request.setAttribute("page", page);
		request.setAttribute("pageRows", pageRows);
		request.setAttribute("writePages", writePages);
		request.setAttribute("totalCnt", totalCnt);
		request.setAttribute("totalPage", totalPage);

//		responseJSON(request, response);
//		AjaxWriteList result = new AjaxWriteList(); // AjaxWriteList 객체 생성해주고
//
//		result.setStatus((String) request.getAttribute("status"));
//		result.setMessage((String) request.getAttribute("message"));
//
//		if (fileList != null) {
//			result.setCount(fileList.length);
//			result.setList(Arrays.asList(fileList));
//		}
//
//		// 페이징 할 때 필요한 값들
//		try {
//			result.setPage((Integer) request.getAttribute("page"));
//			result.setTotalPage((Integer) request.getAttribute("totalPage"));
//			result.setWritePages((Integer) request.getAttribute("writePages"));
//			result.setPageRows((Integer) request.getAttribute("pageRows"));
//			result.setTotalCnt((Integer) request.getAttribute("totalCnt"));
//
//		} catch (Exception e) {
//
//		}

		// --------------------------------------------------------
//
//		ObjectMapper mapper = new ObjectMapper(); // Json 매핑할 객체
//
//		// 자바객체를 제이슨 문자열로 바꾸고, 이거를 제이슨 타입으로 변환해줌
//		try {
//			String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
//
//			response.setContentType("application/json; charset=utf-8");
//			response.getWriter().write(jsonString);
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	} // end execute



	
}
