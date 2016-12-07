package com.novsky.map.util;

/**
 * 北斗通讯
 * @author steve
 *
 */
public class BDMessageData {

	private int id;
	
	/**
	 * 用户地址
	 */
	private String  columnsUserAddress="";
	
	/**
	 * 消息类别
	 */
	private String  columnsMsgType="";
	
	/**
	 * 发送地址
	 */
	private String  columnsSendAddress="";
	
	/**
	 * 发送时间
	 */
	private String  columnsSendTime="";
	
	/**
	 * 电文长度
	 */
	private String  columnsMsgLen="";
	
	/**
	 * 电文长度
	 */
	private String  columnsMsgContent="";
	
	/**
	 * crc验证
	 */
	private String  columnsCrc="";
	
	/**
	 *短信标识  0-收件箱  1-发件箱  2-草稿箱  3-表示未读
	 */
	private String  columnsMsgFlag="";
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getColumnsUserAddress() {
		return columnsUserAddress;
	}
	
	public void setColumnsUserAddress(String columnsUserAddress) {
		this.columnsUserAddress = columnsUserAddress;
	}
	
	public String getColumnsMsgType() {
		return columnsMsgType;
	}
	
	public void setColumnsMsgType(String columnsMsgType) {
		this.columnsMsgType = columnsMsgType;
	}
	
	public String getColumnsSendAddress() {
		return columnsSendAddress;
	}
	
	public void setColumnsSendAddress(String columnsSendAddress) {
		this.columnsSendAddress = columnsSendAddress;
	}
	
	public String getColumnsSendTime() {
		return columnsSendTime;
	}
	
	public void setColumnsSendTime(String columnsSendTime) {
		this.columnsSendTime = columnsSendTime;
	}
	
	public String getColumnsMsgLen() {
		return columnsMsgLen;
	}
	
	public void setColumnsMsgLen(String columnsMsgLen) {
		this.columnsMsgLen = columnsMsgLen;
	}
	
	public String getColumnsMsgContent() {
		return columnsMsgContent;
	}
	
	public void setColumnsMsgContent(String columnsMsgContent) {
		this.columnsMsgContent = columnsMsgContent;
	}
	
	public String getColumnsCrc() {
		return columnsCrc;
	}
	
	public void setColumnsCrc(String columnsCrc) {
		this.columnsCrc = columnsCrc;
	}
	
	public String getColumnsMsgFlag() {
		return columnsMsgFlag;
	}
	
	public void setColumnsMsgFlag(String columnsMsgFlag) {
		this.columnsMsgFlag = columnsMsgFlag;
	}
}
