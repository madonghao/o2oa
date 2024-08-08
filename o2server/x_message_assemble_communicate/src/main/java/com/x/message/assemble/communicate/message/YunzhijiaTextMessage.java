package com.x.message.assemble.communicate.message;

public class YunzhijiaTextMessage extends YunzhijiaBaseMessage {

	private static final long serialVersionUID = 8600141025638957869L;

	public YunzhijiaTextMessage(String no, String pub, String pubSecret) {
		super(no, pub, pubSecret);
		this.type = 2;
	}

	public void setText(String text) {
		this.msg.addProperty("text", text);
	}
}
