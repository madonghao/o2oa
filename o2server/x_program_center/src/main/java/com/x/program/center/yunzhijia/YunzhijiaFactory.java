package com.x.program.center.yunzhijia;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import com.x.base.core.project.bean.NameValuePair;
import com.x.base.core.project.config.Config;
import com.x.base.core.project.connection.HttpConnection;
import com.x.base.core.project.gson.GsonPropertyObject;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.base.core.project.tools.ListTools;

public class YunzhijiaFactory {

    private static Logger logger = LoggerFactory.getLogger(YunzhijiaFactory.class);

    private String accessToken;

    private List<Department> orgs;

    private List<User> users;

    private List<Company> companys;

    public YunzhijiaFactory(String accessToken) throws Exception {
        this.accessToken = accessToken;
        orgs = this.orgs();
        users = this.users();
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        String dbUrl = "jdbc:sqlserver://" + Config.yunzhijia().getShrDBUrl() + ":1433;databaseName=sHRMaster";
        String dbUser = Config.yunzhijia().getShrDBUser();
        String dbPwd = Config.yunzhijia().getShrDBPassword();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPwd)) {
            Map<String, String> userMap = getUserMap(conn);
            Map<String, String> unitMap = getUnitMap(conn);

            updateUserInfo(userMap);
            updateOrgInfo(unitMap);
        }
        users = ListTools.trim(users, true, true);
        companys = this.companys();
    }

    private Map<String, String> getUserMap(Connection conn) throws SQLException {
        String userSql = "SELECT FNumber, FUid, FCell FROM T_PM_User WHERE FCell IS NOT NULL AND FIsDelete = '0'";
        Map<String, String> userMap = new HashMap<>();

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(userSql)) {
            while (rs.next()) {
                String num = rs.getString("FNumber");
                String cell = rs.getString("FCell");
                userMap.put(cell, num);
            }
        }

        return userMap;
    }

    private Map<String, String> getUnitMap(Connection conn) throws SQLException {
        String unitSql = "SELECT FNumber, FDisplayName_L2 FROM T_ORG_Admin UNION SELECT FNumber, FDisplayName_L2 FROM Custom_ST_Unit";
        Map<String, String> unitMap = new HashMap<>();

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(unitSql)) {
            while (rs.next()) {
                String num = rs.getString("FNumber");
                String uid = rs.getString("FDisplayName_L2").replaceFirst("盛腾集团_", "");
                unitMap.put(uid, num);
            }
        }

        return unitMap;
    }

    private void updateUserInfo(Map<String, String> userMap) {
        for (User user : users) {
            String fNumber = userMap.get(user.getPhone());
            if (fNumber != null) {
                user.setUnique(fNumber);
            }
        }
    }

    private void updateOrgInfo(Map<String, String> unitMap) {
        for (Department org : orgs) {
            String tempStr = org.getDepartment().replace("\\", "_");
            String fNumber = unitMap.get(tempStr);
            if (fNumber != null) {
                org.setUnique(fNumber);
            }
        }
    }

    public List<Department> getOrgs(){ return this.orgs; }

    public List<Department> roots() {
        return orgs.stream().filter(o -> null == o.getParentId() || o.getParentId().isEmpty()).collect(Collectors.toList());
    }

    private List<Department> orgs() throws Exception {
        logger.info("开始请求部门数据");
        String post = "nonce=" + UUID.randomUUID() + "&eid=" + Config.yunzhijia().getEid();
        List<NameValuePair> heads = new ArrayList<>();
        heads.add(new NameValuePair("Content-Type", "application/x-www-form-urlencoded"));
        String address = Config.yunzhijia().getApiAddress() + "/gateway/openimport/open/dept/getall?accessToken="
                + this.accessToken;
        logger.info("请求地址:" + address);
        OrgListResp resp =  HttpConnection.postAsObject(address, heads, post, OrgListResp.class);
        logger.debug("orgs response:{}.", resp);
        if (resp.getErrorCode() != 100) {
            throw new ExceptionListOrg(resp.getErrorCode(), resp.getError());
        }
        return resp.getData();
    }

    private List<User> users() throws Exception {
        logger.info("开始请求人员数据");
        String post = "nonce=" + UUID.randomUUID() + "&eid=" + Config.yunzhijia().getEid() + "&data={}";
        List<NameValuePair> heads = new ArrayList<>();
        heads.add(new NameValuePair("Content-Type", "application/x-www-form-urlencoded"));
        String address = Config.yunzhijia().getApiAddress() + "/gateway/openimport/open/person/getall?accessToken="
                + this.accessToken;
        UserListResp resp = HttpConnection.postAsObject(address, heads, post, UserListResp.class);
        logger.debug("users response:{}.", resp);
        if (resp.getErrorCode() != 100) {
            throw new ExceptionListUser(resp.getErrorCode(), resp.getError());
        }
        return resp.getData();
    }

    private List<Company> companys() throws Exception {
        logger.info("开始请求兼职数据");
        String post = "nonce=" + UUID.randomUUID() + "&eid=" + Config.yunzhijia().getEid() + "&data={}";
        List<NameValuePair> heads = new ArrayList<>();
        heads.add(new NameValuePair("Content-Type", "application/x-www-form-urlencoded"));
        String address = Config.yunzhijia().getApiAddress() + "/gateway/openimport/open/company/queryPartTimeJobs?accessToken="
                + this.accessToken;
        CompanyListResp resp = HttpConnection.postAsObject(address, heads, post, CompanyListResp.class);
        logger.debug("companys response:{}.", resp);
        if (resp.getErrorCode() != 100) {
            throw new ExceptionListUser(resp.getErrorCode(), resp.getError());
        }
        return resp.getData();
    }

    public List<User> listUser(Department org) throws Exception {
        return users.stream().filter(o -> Objects.equals(o.getOrgId(), org.getId()))
                .collect(Collectors.toList());
    }

    public List<Company> getCompanys(){ return this.companys; }

    public List<Department> listSub(Department org) throws Exception {
        return orgs.stream().filter(o -> {
                    return Objects.equals(o.getParentId(), org.getId());
                }).sorted(Comparator.comparing(Department::getWeights, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    public static class OrgListResp extends GsonPropertyObject {

        private boolean success;
        private String error;
        private int errorCode;
        private List<Department> data;

        public void setSuccess(boolean success) {
            this.success = success;
        }
        public boolean getSuccess() {
            return success;
        }

        public void setError(String error) {
            this.error = error;
        }
        public String getError() {
            return error;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }
        public int getErrorCode() {
            return errorCode;
        }

        public void setData(List<Department> data) {
            this.data = data;
        }
        public List<Department> getData() {
            return data;
        }
    }

    public static class UserListResp extends GsonPropertyObject {

        private boolean success;
        private String error;
        private int errorCode;
        private List<User> data;
        public void setSuccess(boolean success) {
            this.success = success;
        }
        public boolean getSuccess() {
            return success;
        }

        public void setError(String error) {
            this.error = error;
        }
        public String getError() {
            return error;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }
        public int getErrorCode() {
            return errorCode;
        }

        public void setData(List<User> data) {
            this.data = data;
        }
        public List<User> getData() {
            return data;
        }
    }

    public static class CompanyListResp extends GsonPropertyObject {

        private boolean success;
        private String error;
        private int errorCode;
        private List<Company> data;
        public void setSuccess(boolean success) {
            this.success = success;
        }
        public boolean getSuccess() {
            return success;
        }

        public void setError(String error) {
            this.error = error;
        }
        public String getError() {
            return error;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }
        public int getErrorCode() {
            return errorCode;
        }

        public void setData(List<Company> data) {
            this.data = data;
        }
        public List<Company> getData() {
            return data;
        }
    }
}