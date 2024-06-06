package com.x.organization.assemble.authentication.jaxrs.yunzhijia;

import com.x.base.core.project.exception.PromptException;

public class ExceptionPersonNotExist extends PromptException {

    private static final long serialVersionUID = 4132300948670472899L;

    ExceptionPersonNotExist(String name) {
        super("person:{} not exist.", name);
    }
}
