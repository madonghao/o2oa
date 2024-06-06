package com.x.organization.assemble.authentication.jaxrs.yunzhijia;

import com.x.base.core.project.exception.PromptException;

class ExceptionCodeEmpty extends PromptException {

    private static final long serialVersionUID = 4132300948670472899L;

    ExceptionCodeEmpty() {
        super("云之家登录code不能为空.");
    }
}