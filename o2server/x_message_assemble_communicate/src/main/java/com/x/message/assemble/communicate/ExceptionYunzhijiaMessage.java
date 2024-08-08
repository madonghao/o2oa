package com.x.message.assemble.communicate;

import com.x.base.core.project.exception.PromptException;

class ExceptionYunzhijiaMessage extends PromptException {

	private static final long serialVersionUID = 4132300948670472899L;

	ExceptionYunzhijiaMessage(Integer retCode, String retMessage) {
		super("发送云之家消息失败,错误代码:{},错误消息:{}.", retCode, retMessage);
	}
}
