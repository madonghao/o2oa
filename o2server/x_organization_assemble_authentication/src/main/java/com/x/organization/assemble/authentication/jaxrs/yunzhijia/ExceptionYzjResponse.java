package com.x.organization.assemble.authentication.jaxrs.yunzhijia;

import com.x.base.core.project.exception.PromptException;

public class ExceptionYzjResponse extends PromptException {
    private static final long serialVersionUID = -2068458307623732091L;

    ExceptionYzjResponse(Integer retCode, String retMessage) {
        super("云之家单点失败,错误代码:{},错误消息:{}.", retCode, retMessage);
    }
}
