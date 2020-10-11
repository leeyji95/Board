package com.imoxion.board.beans;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Key {
	private static final Logger logger = LoggerFactory.getLogger(Key.class);
	

	public Key() { // 싱글톤 
		logger.info("Key() 생성");
	}
	
	private static Key instance = null;
	
	// 싱글톤(인스턴스 하나만)
	public static Key getInstance() {
		if (instance == null) {
			instance = new Key(); // 인스턴스 생성
		}
		return instance;
	}
	
	public int createKey() {
		
		AtomicInteger wkey = new AtomicInteger();
		return wkey.incrementAndGet(); // 현재 값 얻고, 1 증가 
	}
	
	
	
//	
//	static int wkey = 1; // writeKey : 저장된 글 구분하는 key 값
//	
//	public int createWkey() {
//		return wkey++;
//	}
//	
//	// getter & setter
//	public int getWkey() {return wkey;}
//	public void setWkey(int wkey) {Key.wkey = wkey;}
//	
	

	
}
