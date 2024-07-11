package com.x.program.center.yunzhijia;

import com.x.base.core.project.gson.GsonPropertyObject;

import java.util.List;

public class Company extends GsonPropertyObject {
    //  "openId": "65af9d45e4b0fa14e11f282c",
//  "jobTitle": "分公司总经理",
//  "weights": 2147483647,
//  "orgId": "443da3b4-75b5-41e5-ba55-cfd3e7c9e484"
    private String openId;
    private String jobTitle;
    private String weights;
    private String orgId;

    public void setOpenId(String openId) {
        this.openId = openId;
    }
    public String getOpenId() {
        return openId;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    public String getJobTitle() {
        return jobTitle;
    }

    public void setWeights(String weights) {
        this.weights = weights;
    }
    public String getWeights() {
        return weights;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
    public String getOrgId() {
        return orgId;
    }
}
