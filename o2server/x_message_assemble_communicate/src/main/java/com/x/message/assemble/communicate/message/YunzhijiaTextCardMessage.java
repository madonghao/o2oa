package com.x.message.assemble.communicate.message;

public class YunzhijiaTextCardMessage extends YunzhijiaBaseMessage {

	private static final long serialVersionUID = 3587675274646612547L;

	public YunzhijiaTextCardMessage(String no, String pub, String pubSecret) {
		super(no, pub, pubSecret);
		this.type = 5;
	}

	public void setAppId(String appId) {
		this.msg.addProperty("appid", appId);
	}

	public void setText(String text) {
		this.msg.addProperty("text", text);
	}

	public void setTodo(int todo) {
		this.msg.addProperty("todo", todo);
	}

	public void setUrl(String url) {
		this.msg.addProperty("url", url);
	}
}
