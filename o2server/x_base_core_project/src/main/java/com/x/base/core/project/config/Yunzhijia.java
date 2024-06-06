package com.x.base.core.project.config;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.x.base.core.project.annotation.FieldDescribe;
import com.x.base.core.project.connection.HttpConnection;
import com.x.base.core.project.gson.GsonPropertyObject;

public class Yunzhijia extends ConfigObject {

    private static Logger logger = LoggerFactory.getLogger(Yunzhijia.class);

    @FieldDescribe("是否启用.")
    private Boolean enable;
    @FieldDescribe("api服务器地址")
    private String apiAddress;
    @FieldDescribe("云之家AppSecret")
    private String appSecret;
    @FieldDescribe("通讯录Secret")
    private String addressBookSecret;
    @FieldDescribe("云之家AppID")
    private String appId;
    @FieldDescribe("云之家团队EID")
    private String eid;
    @FieldDescribe("云之家SignKey")
    private String signKey;
    @FieldDescribe("云之家消息打开工作的url地址，如：https://sample.o2oa.net/x_desktop/")
    private String workUrl = "";
    @FieldDescribe("云之家消息处理完成后跳转到特定的门户页面的Id")
    private String messageRedirectPortal = "";
    @FieldDescribe("推送消息到云之家")
    private Boolean messageEnable;
    @FieldDescribe("云之家扫码登录")
    private Boolean scanLoginEnable;

    public static Yunzhijia defaultInstance() { return new Yunzhijia(); }

    public static final Boolean default_enable = false;
    public static final String default_apiAddress = "https://yunzhijia.com";
    public static final String default_appSecret = "";
    public static final String default_addressBookSecret = "";
    public static final String default_appId = "";
    public static final String default_eid = "";
    public static final String default_signKey = "";
    public static final String default_workUrl = "";
    public static final String default_messageRedirectPortal = "";
    public static final Boolean default_messageEanble = false;
    public static final Boolean default_scanLoginEnable = false;

    public Yunzhijia() {
        this.enable = default_enable;
        this.apiAddress = default_apiAddress;
        this.appId = default_appId;
        this.eid = default_eid;
        this.appSecret = default_appSecret;
        this.addressBookSecret = default_addressBookSecret;
        this.signKey = default_signKey;
        this.messageEnable = default_messageEanble;
        this.workUrl = default_workUrl;
        this.messageRedirectPortal = default_messageRedirectPortal;
        this.scanLoginEnable = default_scanLoginEnable;
    }

    private static String cachedCorpAccessToken;
    private static Date cachedCorpAccessTokenDate;

    private static String cachedResAccessToken;
    private static Date cachedResAccessTokenDate;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Boolean getEnable() {
        return BooleanUtils.isTrue(this.enable);
    }

    public Boolean getMessageEnable() {
        return BooleanUtils.isTrue(this.messageEnable);
    }

    public String getWorkUrl() { return StringUtils.isEmpty(this.workUrl) ? default_workUrl : this.workUrl; }

    public String getAppId() { return StringUtils.isEmpty(this.appId) ? default_appId : this.appId; }

    public String getAppSecret() {
        return StringUtils.isEmpty(this.appSecret) ? default_appSecret : this.appSecret;
    }

    public String getSignKey() {
        return StringUtils.isEmpty(this.signKey) ? default_signKey : this.signKey;
    }

    public String getEid() { return StringUtils.isEmpty(this.eid) ? default_eid : this.eid; }

    public String getApiAddress() { return StringUtils.isEmpty(this.apiAddress) ? default_apiAddress : this.apiAddress; }

    public String corpAccessToken() throws Exception {
        if ((StringUtils.isNotEmpty(cachedCorpAccessToken) && (null != cachedCorpAccessTokenDate))
                && (cachedCorpAccessTokenDate.after(new Date()))) {
            return cachedCorpAccessToken;
        } else {
            YzjPost post = new YzjPost();
            post.setAppId(Config.yunzhijia().appId);
            post.setSecret(Config.yunzhijia().appSecret);
            post.setTimestamp(System.currentTimeMillis());
            post.setScope("app");
            String address = default_apiAddress + "/gateway/oauth2/token/getAccessToken";
            logger.info(post.toString());
            AccessTokenResp resp = HttpConnection.postAsObject(address, null, post.toString(), Yunzhijia.AccessTokenResp.class);
            if (resp.getErrcode() != 0) {
                throw new ExceptionYunzhijiaAccessToken(resp.getErrcode(), resp.getErrmsg());
            }
            cachedCorpAccessToken = resp.getExpires_in().getAccess_token();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, resp.getExpires_in().getExpires_in());
            cachedCorpAccessTokenDate = cal.getTime();
            return cachedCorpAccessToken;
        }
    }

    public String resAccessToken() throws Exception {
        if ((StringUtils.isNotEmpty(cachedResAccessToken) && (null != cachedResAccessTokenDate))
                && (cachedResAccessTokenDate.after(new Date()))) {
            return cachedResAccessToken;
        } else {
            YzjPost post = new YzjPost();
            post.setAppId(Config.yunzhijia().appId);
            post.setEid(Config.yunzhijia().eid);
            post.setSecret(Config.yunzhijia().addressBookSecret);
            post.setTimestamp(System.currentTimeMillis());
            post.setScope("resGroupSecret");
            String address = default_apiAddress + "/gateway/oauth2/token/getAccessToken";
            System.out.println("address：" + address);
            AccessTokenResp resp = HttpConnection.postAsObject(address, null, post.toString(), Yunzhijia.AccessTokenResp.class);
            if (resp.getErrcode() != 0) {
                throw new ExceptionYunzhijiaAccessToken(resp.getErrcode(), resp.getErrmsg());
            }
            cachedResAccessToken = resp.getExpires_in().getAccess_token();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, resp.getExpires_in().getExpires_in());
            cachedResAccessTokenDate = cal.getTime();
            return cachedResAccessToken;
        }
    }

    public static class YzjPost extends GsonPropertyObject {
        //{
        //   "appId": 500926751,
        //   "secret": TyKeUwqfOqfNA6BuZusR,
        //   "timestamp": 1708304201160,
        //   "scope": app
        //}

        private String appId;
        private String eid;
        private String secret;
        private Long timestamp;
        private String scope;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getEid() {
            return eid;
        }

        public void setEid(String eid) { this.eid = eid; }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) { this.secret = secret; }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

        public String getScope() { return scope; }

        public void setScope(String scope) { this.scope = scope; }
    }

    public static class AccessTokenResp extends GsonPropertyObject {

        // {
        //    "data": {
        //        "accessToken": "PUwrzk7Ka2KzzaMYUMK2OA7PnJxllGXf",
        //        "expireIn": 7200,
        //        "refreshToken": "rK2AbCYxc7TukhKFOJbvjiMkmwKaGDtS"
        //    },
        //    "error": null,
        //    "errorCode": 0,
        //    "success": true
        //}

        private Integer errorCode;
        private String error;
        private Boolean success;
        private AccessTokenData data;

        public Integer getErrcode() {
            return errorCode;
        }

        public void setErrcode(Integer errorCode) {
            this.errorCode = errorCode;
        }

        public Boolean getSuccess() { return success; }

        public void setSuccess(Boolean success) { this.success = success; }

        public String getErrmsg() {
            return error;
        }

        public void setErrmsg(String error) {
            this.error = error;
        }

        public AccessTokenData getExpires_in() {
            return data;
        }

        public void setExpires_in(AccessTokenData data) { this.data = data; }

    }

    public static class AccessTokenData extends GsonPropertyObject {
        //    "data": {
        //        "accessToken": "PUwrzk7Ka2KzzaMYUMK2OA7PnJxllGXf",
        //        "expireIn": 7200,
        //        "refreshToken": "rK2AbCYxc7TukhKFOJbvjiMkmwKaGDtS"
        //    },
        private String accessToken;
        private Integer expireIn;
        private String refreshToken;

        public String getAccess_token() {
            return accessToken;
        }

        public void setAccess_token(String accessToken) {
            this.accessToken = accessToken;
        }

        public Integer getExpires_in() {
            return expireIn;
        }

        public void setExpires_in(Integer expireIn) {
            this.expireIn = expireIn;
        }

        public String getRefresh_token() {
            return refreshToken;
        }

        public void setRefresh_token(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
