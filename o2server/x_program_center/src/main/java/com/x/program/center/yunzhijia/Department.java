package com.x.program.center.yunzhijia;

import com.x.base.core.project.gson.GsonPropertyObject;

public class Department extends GsonPropertyObject {
    // "name": "开发部",  //组织名称
    // "id": "58547b46-76d6-4d7d-ade0-be91d396cfe4",  //组织的id
    // "weights": 101000,  //排序码
    // "department": "研发中心\\移动平台产品部\\开发部",  //组织长名称
    // "parentId": "58b62641-84bf-406b-a94b-d3bfffae22aa"  //组织父Id
    // "businessUnit":1 //是否是业务单元，1：业务单元，0：非业务单元

    private Integer businessUnit;
    private String name;
    private String id;
    private Long weights;
    private String department;
    private String parentId;
    private String unique;

    public void setUnique(String unique) { this.unique = unique; }
    public String getUnique() {
        return unique;
    }

    public void setBusinessUnit(Integer businessUnit) {
        this.businessUnit = businessUnit;
    }
    public Integer getBusinessUnit() {
        return businessUnit;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }

    public void setWeights(Long weights) {
        this.weights = weights;
    }
    public Long getWeights() {
        return weights;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
    public String getDepartment() {
        return department;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    public String getParentId() {
        return parentId;
    }
}
