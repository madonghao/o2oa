package com.x.organization.assemble.authentication.jaxrs;

import javax.servlet.annotation.WebFilter;

import com.x.base.core.project.jaxrs.AnonymousCipherManagerUserJaxrsFilter;

@WebFilter(urlPatterns = "/jaxrs/yunzhijia/*", asyncSupported = true)
public class YunzhijiaJaxrsFilter extends AnonymousCipherManagerUserJaxrsFilter {

}