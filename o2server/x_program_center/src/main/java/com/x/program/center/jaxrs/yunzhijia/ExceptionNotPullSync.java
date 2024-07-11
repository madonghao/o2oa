package com.x.program.center.jaxrs.yunzhijia;

import com.x.base.core.project.exception.LanguagePromptException;

public class ExceptionNotPullSync extends LanguagePromptException {

    private static final long serialVersionUID = -3439770681867963457L;

    ExceptionNotPullSync() {
        super("没有启用从云之家的拉入同步.");
    }
}
