package com.imoxion.board.beans;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imoxion.board.controller.AjaxController;

public class BoardInfo {
	public static final Logger logger = LoggerFactory.getLogger(BoardInfo.class);

	File f = new File(AjaxController.DIR_PATH);
	String subject = null;
	String name = null;
	String content = null;
	String regdate = null;
	int wkey = 0;

	List<Integer> keySort = new ArrayList<Integer>();

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

	public List<Integer> getKeySort() {
		return keySort;
	}

	public void setKeySort(List<Integer> keySort) {
		this.keySort = keySort;
	}

	/**
	 * 마지막 wkey 글번호 출력
	 * 
	 * @return
	 */
	public int lastWkey() {

		logger.info("lastWkey() 호출");

		String[] nameList = f.list();

		for (int i = 0; i < nameList.length; i++) {
			logger.info("nameList[" + i + "] - " + nameList[i]);
			// 번호만 뽑아서 리스트에 담기
			keySort.add(Integer.parseInt(nameList[i].substring(0, nameList[i].indexOf("_")))); // 리스트에 파일명 저장
		}

		// 번호 정렬
		Collections.sort(keySort);

		// 확인용
		for (int i = 0; i < keySort.size(); i++) {
			logger.info("keySort[" + i + "] - " + keySort.get(i));
		}

		// wkey 세팅
		if (nameList.length == 0) {
			return wkey = 0;
		} else {
			for (int i = keySort.size() - 1; i < keySort.size(); i++) {
				// 정렬된 가장 마지막 번호 가져오기
				wkey = keySort.get(i);

				// 글번호 세팅
				this.setWkey(wkey);
			}
			logger.info("마지막 글 wkey 값 ? - " + wkey);
		}
		return wkey;
	}

	/**
	 * 글 번호 오름차순 정렬 및 리스트에 저장
	 * 
	 * @return
	 */
	public List<Integer> Sort() {

		logger.info("Sort() 호출");

		if (keySort.size() == 0) {

			String[] fNnameList = f.list();

			for (int i = 0; i < fNnameList.length; i++) {
				// 번호만 뽑아서 리스트에 담기
				keySort.add(Integer.parseInt(fNnameList[i].substring(0, fNnameList[i].indexOf("_")))); // 리스트에 파일명 저장
			}

			// 번호 정렬 + 저장
			Collections.sort(keySort);
			this.setKeySort(keySort);

			// 확인용
			for (int i = 0; i < keySort.size(); i++) {
				logger.info("rowNum() - update_keySort - " + keySort.get(i));
			}

			return this.getKeySort();
		} else {
			return this.getKeySort();
		}
	}

	/**
	 * 글 목록 출력(10개씩)
	 * 
	 * @param f
	 */
	public List<BoardInfo> listFile(int from, int end) {

		logger.info("listFile() 호출");

		List<BoardInfo> list = new ArrayList<BoardInfo>();
		try {
			// 10개씩 뽑기
			for (int i = from; i < end; i++) {
				logger.info("keySort[" + i + "] - " + keySort.get(i));

				wkey = keySort.get(i);

				FilenameFilter filter = new FilenameFilter() {

					public boolean accept(File curDir, String name) {
						return name.startsWith(String.valueOf(wkey));
					}
				};

				// 해당 글번호를 가지고 있는 파일명 뽑기
				String[] targetFile = f.list(filter);

				// 제목
				int startIndex = targetFile[0].indexOf("_");
				int endIndex = targetFile[0].indexOf(".txt");
				subject = targetFile[0].substring(startIndex + 1, endIndex).trim();

				// 작성자, 내용
				try (BufferedReader br = new BufferedReader(
						new FileReader(new File(AjaxController.DIR_PATH, targetFile[0])))) {
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
			} // end for

		} catch (IndexOutOfBoundsException e) {
			// 예외처리하지 않음...
		}

		return list;
	} // end nameList()

	/**
	 * 특정 글 조회하기
	 * 
	 * @param wkey
	 * @return
	 */
	public List<BoardInfo> ViewFile(String wkey) {

		logger.info("ViewFile() 호출");

		List<BoardInfo> dataArr = new ArrayList<BoardInfo>();

		FilenameFilter filter = new FilenameFilter() {

			public boolean accept(File curDir, String name) {
				return name.startsWith(wkey);
			}
		};

		// 해당 글번호를 가지고 있는 파일명 뽑기
		String[] targetFile = f.list(filter);

		// 제목
		int startIndex = targetFile[0].indexOf("_");
		int endIndex = targetFile[0].indexOf(".txt");
		subject = targetFile[0].substring(startIndex + 1, endIndex).trim();

		// 작성자, 내용
		try (BufferedReader br = new BufferedReader(new FileReader(new File(AjaxController.DIR_PATH, targetFile[0])))) {
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

			// 날짜
			startIndex = sb.toString().indexOf("날짜");
			int endIndex2 = sb.toString().indexOf("작성자");
			regdate = sb.toString().substring(startIndex + 4, endIndex2).trim();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		dataArr.add(new BoardInfo(subject, name, content, regdate, Integer.parseInt(wkey)));

		return dataArr;
	}

	
	/**
	 * 글 수정 처리
	 * @param subject
	 * @param content
	 * @param wkey
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public int UpdateFile(String subject, String content, String wkey) throws Exception, IOException {

		String name = null;
		int count = 0;
		
		FilenameFilter filter = new FilenameFilter() {

			public boolean accept(File curDir, String name) {
				return name.startsWith(wkey);
			}
		};

		// 해당 글번호를 가지고 있는 파일명 뽑기
		String[] targetFile = f.list(filter);

		// 해당 파일에서 작성자만 뽑고, 삭제 후 같은 글번호로 파일 생성한다.
		try (BufferedReader br = new BufferedReader(new FileReader(new File(AjaxController.DIR_PATH, targetFile[0])))) {
			String line;
			StringBuffer sb = new StringBuffer();

			while ((line = (br.readLine())) != null) {
				sb.append(line + "\n");
			}

			// 작성자
			int startIndex = sb.toString().indexOf("]");
			int endIndex = sb.toString().indexOf("제목");
			name = sb.toString().substring(startIndex + 2, endIndex).trim();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// 해당 파일 삭제
		File[] f = AjaxController.f.listFiles(filter);
		if (f[0].exists()) { // 만약 해당 파일 존재하면,
			f[0].delete(); // 삭제

		// 파일 이름 다시 생성
			String fName = wkey + "_" + subject + ".txt";
			File updateFile = new File(AjaxController.DIR_PATH, fName); // 해당 경로에 파일 객체 생성

			if (!updateFile.exists()) {
				if (updateFile.createNewFile()) {

					DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
					String regdate = dateFormat.format(new Date());

					// 파일에 쓰고 저장
					try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(updateFile)))) {

						// 데이터 쓰고 저장하기
						out.println("날짜 : " + regdate);
						out.println("작성자 ] " + name);
						out.println("제목: " + subject);
						out.println("내용: " + content);
						out.flush();

						count = 1;
					} catch (IOException e) {
						logger.error("데이터 저장 실패");
					}
				}
			} else {
				logger.info("업데이튼 된 파일 존재");
			}
		} 
		return count;
	} // end UpdateFile()

}
