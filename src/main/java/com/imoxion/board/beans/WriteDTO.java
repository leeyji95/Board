package com.imoxion.board.beans;

public class WriteDTO {
	
	private int wkey;
	private String subject;
	private String name;
	private String regdate;
	
	public WriteDTO() {
		super();
	}
	
	public int getWkey() {
		return wkey;
	}
	public void setWkey(int wkey) {
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
	public String getRegdate() {
		return regdate;
	}
	public void setRegdate(String regdate) {
		this.regdate = regdate;
	}

	
}
