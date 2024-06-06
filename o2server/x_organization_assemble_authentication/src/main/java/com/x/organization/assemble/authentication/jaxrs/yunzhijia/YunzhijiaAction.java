package com.x.organization.assemble.authentication.jaxrs.yunzhijia;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.x.base.core.project.annotation.JaxrsDescribe;
import com.x.base.core.project.annotation.JaxrsMethodDescribe;
import com.x.base.core.project.annotation.JaxrsParameterDescribe;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.http.HttpMediaType;
import com.x.base.core.project.jaxrs.ResponseFactory;
import com.x.base.core.project.jaxrs.StandardJaxrsAction;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.organization.assemble.authentication.jaxrs.yunzhijia.ActionLogin;
import com.x.organization.assemble.authentication.jaxrs.yunzhijia.YunzhijiaAction;

@Path("yunzhijia")
@JaxrsDescribe("云之家单点登录")
public class YunzhijiaAction extends StandardJaxrsAction {
    private static Logger logger = LoggerFactory.getLogger(YunzhijiaAction.class);

    @JaxrsMethodDescribe(value = "云之家单点登录.", action = ActionLogin.class)
    @GET
    @Path("ticket/{ticket}")
    @Produces(HttpMediaType.APPLICATION_JSON_UTF_8)
    @Consumes(MediaType.APPLICATION_JSON)
    public void getLogin(@Suspended final AsyncResponse asyncResponse, @Context HttpServletRequest request,
                         @Context HttpServletResponse response, @JaxrsParameterDescribe("云之家ticket码") @PathParam("ticket") String ticket) {
        ActionResult<ActionLogin.Wo> result = new ActionResult<>();
        EffectivePerson effectivePerson = this.effectivePerson(request);
        try {
            result = new ActionLogin().execute(request, response, effectivePerson, ticket);
        } catch (Exception e) {
            logger.error(e, effectivePerson, request, null);
            result.error(e);
        }
        asyncResponse.resume(ResponseFactory.getEntityTagActionResultResponse(request, result));
    }
}
