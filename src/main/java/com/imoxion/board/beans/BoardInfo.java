package com.imoxion.board.beans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imoxion.board.controller.AjaxController;

public class BoardInfo {
	public static final Logger logger = LoggerFactory.getLogger(BoardInfo.class);

	File f = new File(AjaxController.DIR_PATH);
	String subject = null, name = null, content = null, regdate = null;
	int wkey = 0;
	List<BoardInfo> list = new ArrayList<BoardInfo>();

	public BoardInfo() {
		super();
		logger.info("BoardInfo() 기본 생성자 생성");
	}

	public BoardInfo(String subject, String name, String content, String regdate, int wkey) {
		super();
		this.subject = subject;
		this.name = name;
		this.content = content;
		this.regdate = regdate;
		this.wkey = wkey;
	}

	/**
	 * 글 목록 출력
	 * 
	 * @param f
	 */
	public void listFile(File f) {

		logger.info("listFile() 호출");

		String[] nameList = f.list();

		for (int i = 0; i < nameList.length; i++) {
			logger.info("파일명" + (i + 1) + "번째 - " + nameList[i]);

			// 글번호
			wkey = Integer.parseInt(nameList[i].substring(0, nameList[i].indexOf("_")));

			// 제목
			int startIndex = nameList[i].indexOf("_");
			int endIndex = nameList[i].indexOf(".txt");
			subject = nameList[i].substring(startIndex + 1, endIndex).trim();

			// 작성자, 내용
			try (BufferedReader br = new BufferedReader(
					new FileReader(new File(AjaxController.DIR_PATH, nameList[i])))) {
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
				startIndex = sb.toString().indexOf("내용");
				content = sb.toString().substring(startIndex + 3).trim();

				// 날짜
				startIndex = sb.toString().indexOf("날짜");
				int endIndex2 = sb.toString().indexOf("작성자");
				regdate = sb.toString().substring(startIndex + 4, endIndex2).trim();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			list.add(new BoardInfo(subject, name, content, regdate, wkey));
		}

	} // end nameList()

	/**
	 * 마지막 wkey 글번호 출력
	 * 
	 * @return
	 */
	public int lastWkey() {

		logger.info("lastWkey() 호출");

		String[] nameList = f.list();
		List<Integer> arrlist = new ArrayList<Integer>();
		
		for (int i = 0; i < nameList.length; i++) {
			logger.info("nameList[" + i + "] - " + nameList[i]);
			arrlist.add(Integer.parseInt(nameList[i].substring(0, nameList[i].indexOf("_")))); // 리스트에 파일명 저장
		}
		
		Collections.sort(arrlist);
		for (int i = 0; i < arrlist.size(); i++) {
			logger.info("arrlist[" + i + "] - " + arrlist.get(i));
		}
		
		
		
//
//			Arrays.sort(indx);
//			for(int k = 0 ; k < indx.length; k++) {
//				logger.info("k - " + indx[k]);
//			}

		if (nameList.length == 0) {

			return wkey = 0;

		} else {

			for (int i = nameList.length - 1; i < nameList.length; i++) {
				// 글번호
				wkey = Integer.parseInt(nameList[i].substring(0, nameList[i].indexOf("_")));
				this.setWkey(wkey);
			}

			// 파일 개수
			
			
			
			
			
			logger.info("마지막 글 wkey 값 ? - " + wkey);
		}

		return wkey;
	}

	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRegdate() {
		return regdate;
	}

	public void setRegdate(String regdate) {
		this.regdate = regdate;
	}

	public int getWkey() {
		return wkey;
	}

	public void setWkey(int wkey) {
		this.wkey = wkey;
	}

	public List<BoardInfo> getList() {
		return list;
	}

	public void setList(List<BoardInfo> list) {
		this.list = list;
	}

	@Override
	public String toString() {
		return "제목 : " + subject + " 작성자 : " + name + " 내용 : " + content + " 날짜 : " + regdate + " 글번호 : " + wkey;
	}

}
