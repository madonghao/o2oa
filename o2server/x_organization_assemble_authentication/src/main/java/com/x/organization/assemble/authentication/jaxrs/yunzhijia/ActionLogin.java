package com.x.organization.assemble.authentication.jaxrs.yunzhijia;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.config.Config;
import com.x.base.core.project.connection.HttpConnection;
import com.x.base.core.project.gson.XGsonBuilder;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.http.HttpToken;
import com.x.base.core.project.http.TokenType;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.organization.assemble.authentication.Business;
import com.x.organization.core.entity.Person;

class ActionLogin extends BaseAction {

    private static Logger logger = LoggerFactory.getLogger(ActionLogin.class);

    private Gson gson = XGsonBuilder.instance();

    ActionResult<Wo> execute(HttpServletRequest request, HttpServletResponse response, EffectivePerson effectivePerson,
                             String ticket) throws Exception {
        ActionResult<Wo> result = new ActionResult<>();
        try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
            if (StringUtils.isEmpty(ticket)) {
                throw new ExceptionCodeEmpty();
            }

            String url = Config.yunzhijia().getApiAddress() + "/gateway/ticket/user/acquirecontext?accessToken="
                    + Config.yunzhijia().corpAccessToken();
            logger.debug("yunzhijia url:{}.", url);
            logger.info(url);
            ActionLogin.YunzhijiaGetUserInfoResp resp = HttpConnection.postAsObject(url, null, "{\"appid\":\"" + Config.yunzhijia().getAppId() + "\",\"ticket\":\"" + ticket + "\"}", ActionLogin.YunzhijiaGetUserInfoResp.class);
            logger.info(gson.toJson(resp.getData()));
            if (resp == null || resp.getErrorCode() == null || resp.getErrorCode() != 0) {
                Integer errCode;
                if (resp == null) errCode = -1;
                else errCode = resp.getErrorCode();
                String errMsg = resp == null ? "" : resp.getError();
                throw new ExceptionYzjResponse(errCode, errMsg);
            }
            String openId = resp.getData().getOpenid();
            if (StringUtils.isEmpty(openId)) {
                logger.info("openId为空，无法单点登录！！！");
                throw new ExceptionYzjResponse(resp.getErrorCode(), resp.getError());
            }
            Business business = new Business(emc);
            String personId = business.person().getPersonIdWithYzjId(openId);
            if (StringUtils.isEmpty(personId)) {
                throw new ExceptionPersonNotExist(openId);
            }
            if (personId.indexOf(",") > 0) {
                throw new ExceptionYzjRepeated(openId);
            }
            Person person = emc.find(personId, Person.class);
            ActionLogin.Wo wo = ActionLogin.Wo.copier.copy(person);
            List<String> roles = business.organization().role().listWithPerson(person.getDistinguishedName());
            wo.setRoleList(roles);
            EffectivePerson effective = new EffectivePerson(wo.getDistinguishedName(), TokenType.user,
                    Config.token().getCipher(), Config.person().getEncryptType());
            wo.setToken(effective.getToken());
            HttpToken httpToken = new HttpToken();
            httpToken.setToken(request, response, effective);
            result.setData(wo);
        }
        return result;
    }

    // 定义类
    public static class Wo extends Person {

        private static final long serialVersionUID = 4901269474728548509L;

        public static List<String> Excludes = new ArrayList<>(JpaObject.FieldsInvisible);

        static {
            Excludes.add("password");
        }

        static WrapCopier<Person, ActionLogin.Wo> copier = WrapCopierFactory.wo(Person.class, ActionLogin.Wo.class, null, Excludes);

        private String token;
        private List<String> roleList;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public List<String> getRoleList() {
            return roleList;
        }

        public void setRoleList(List<String> roleList) {
            this.roleList = roleList;
        }
    }

    public static class YunzhijiaGetUserInfoResp {

        /**
         * <code>	 {
         *     "success": true,
         *     "errorCode": 0,
         *     "data": {
         *         "appid": "500926751",
         *         "eid": "25870391",
         *         "openid": "65af13a1e4b07f41d25bd356",
         *         "username": "马东浩",
         *         "uid": "152372806",
         *         "tid": "25870391",
         *         "userid": "659bc128e4b0846b51137468",
         *         "oid": "65af13a1e4b07f41d25bd356",
         *         "networkid": "658150d0e4b03133d6a0aedf",
         *         "xtid": "65af13a1e4b07f41d25bd354",
         *         "ticket": null,
         *         "deviceId": null,
         *         "jobNo": "000302"
         *     },
         *     "error": null
         * }
         * </code>
         */

        private Boolean success;
        private Integer errorCode;
        private UserData data;
        private String error;

        public void setSuccess(boolean success) {
            this.success = success;
        }
        public boolean getSuccess() {
            return success;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }
        public Integer getErrorCode() {
            return errorCode;
        }

        public void setData(UserData data) {
            this.data = data;
        }
        public UserData getData() {
            return data;
        }

        public void setError(String error) {
            this.error = error;
        }
        public String getError() {
            return error;
        }
    }

    public static class UserData {
        /**
         * "data": {
         *     "appid": "500926751",
         *     "eid": "25870391",
         *     "openid": "65af13a1e4b07f41d25bd356",
         *     "username": "马东浩",
         *     "uid": "152372806",
         *     "tid": "25870391",
         *     "userid": "659bc128e4b0846b51137468",
         *     "oid": "65af13a1e4b07f41d25bd356",
         *     "networkid": "658150d0e4b03133d6a0aedf",
         *     "xtid": "65af13a1e4b07f41d25bd354",
         *     "ticket": null,
         *     "deviceId": null,
         *     "jobNo": "000302"
         * }
         */
        private String appid;
        private String eid;
        private String openid;
        private String username;
        private String uid;
        private String tid;
        private String userid;
        private String oid;
        private String networkid;
        private String xtid;
        private String ticket;
        private String deviceId;
        private String jobNo;

        public void setAppid(String appid) {
            this.appid = appid;
        }
        public String getAppid() {
            return appid;
        }

        public void setEid(String eid) {
            this.eid = eid;
        }
        public String getEid() {
            return eid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }
        public String getOpenid() {
            return openid;
        }

        public void setUsername(String username) {
            this.username = username;
        }
        public String getUsername() {
            return username;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }
        public String getUid() {
            return uid;
        }

        public void setTid(String tid) {
            this.tid = tid;
        }
        public String getTid() {
            return tid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }
        public String getUserid() {
            return userid;
        }

        public void setOid(String oid) {
            this.oid = oid;
        }
        public String getOid() {
            return oid;
        }

        public void setNetworkid(String networkid) {
            this.networkid = networkid;
        }
        public String getNetworkid() {
            return networkid;
        }

        public void setXtid(String xtid) {
            this.xtid = xtid;
        }
        public String getXtid() {
            return xtid;
        }

        public void setTicket(String ticket) {
            this.ticket = ticket;
        }
        public String getTicket() {
            return ticket;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }
        public String getDeviceId() {
            return deviceId;
        }

        public void setJobNo(String jobNo) {
            this.jobNo = jobNo;
        }
        public String getJobNo() {
            return jobNo;
        }
    }

}

