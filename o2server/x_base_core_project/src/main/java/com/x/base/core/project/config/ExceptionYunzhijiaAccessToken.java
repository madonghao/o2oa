package com.x.base.core.project.config;

import com.x.base.core.project.exception.PromptException;

class ExceptionYunzhijiaAccessToken extends PromptException {
    private static final long serialVersionUID = -3439770681867963457L;

    ExceptionYunzhijiaAccessToken(Integer code, String message) {
        super("获取云之家access token失败,错误代码:{}, 错误消息:{}.", code, message);
    }
}
