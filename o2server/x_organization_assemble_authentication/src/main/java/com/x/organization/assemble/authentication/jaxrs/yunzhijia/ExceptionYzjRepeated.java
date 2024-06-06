package com.x.organization.assemble.authentication.jaxrs.yunzhijia;

import com.x.base.core.project.exception.PromptException;

public class ExceptionYzjRepeated extends PromptException {

    private static final long serialVersionUID = 4132300948670472899L;

    ExceptionYzjRepeated(String userId) {
        super("查询到重复的云之家openid. {} " ,userId);
    }
}
