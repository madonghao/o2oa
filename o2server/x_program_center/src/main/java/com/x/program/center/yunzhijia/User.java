package com.x.program.center.yunzhijia;

import com.x.base.core.project.gson.GsonPropertyObject;

public class User extends GsonPropertyObject {
    // "birthday": "2012-12-12",
    // "hireDate": "2020-03-18", //入职日期
    // "positiveDate": "2020-06-18", //转正日期
    // "gender": "2",  //性别,0: 不确定; 1: 男; 2: 女
    // "isHidePhone": "0",  //是否在通讯录中隐藏手机号码,0: 不隐藏; 1: 隐藏,默认为0
    // "openId": "5df86ebbe4b0b7058a9efc3d",  //人员的openid
    // "jobTitle": "开发工程师",  // 主职部门的职位,主职部门里多个兼职职位会以英文逗号隔开,例："主职职位1,兼职职位1,兼职职位2"，其中第一个逗号前面的职位（主职职位1）为该人的主职职位.（下同）
    // "weights": "0",
    // "orgId": "4fcfba72-e730-4deb-b327-7a2d5c9443ff",
    // "photoUrl": "http://static.yunzhijia.com/space/c/photo/load?id=5df86ebb6d67ff1f2a97e92b",  //头像URL
    // "uid": "125153990",
    // "phone": "13013013055",  //手机号码
    // "orgUserType": "1",  //是否部门负责人 0:否， 1：是
    // "contact": "[{\"name\":\"邮箱\",\"type\":\"E\",\"publicid\":\"VIRTUAL\",\"permission\":\"R\",\"value\":\"1587954@163.com\"},{\"name\":\"生日\",\"type\":\"O\",\"publicid\":\"VIRTUAL\",\"permission\":\"W\",\"value\":\"2012-12-12\",\"inputType\":\"date\"},{\"name\":\"手机\",\"type\":\"P\",\"publicid\":\"VIRTUAL\",\"permission\":\"R\",\"value\":\"13013013055\"},{\"name\":\"工号\",\"type\":\"O\",\"publicid\":\"4e8cc4c7-08da-469f-a29d-1336b9c1482c\",\"permission\":\"R\",\"value\":\"111131111\"}]",
    // "jobNo": "",  //企业工号
    // "name": "吴**",  //姓名
    // "department": "A产品事业部",  //组织长名称
    // "email": "1587954@163.com",  //邮箱
    // "status": "1"  //状态 0: 注销，1: 正常，2: 禁用

    private String birthday;
    private String hireDate;
    private String gender;
    private String isHidePhone;
    private String openId;
    private String jobTitle;
    private String weights;
    private String orgId;
    private String photoUrl;
    private String uid;
    private String positiveDate;
    private String phone;
    private String orgUserType;
    private String contact;
    private String jobNo;
    private String name;
    private String department;
    private String email;
    private String status;
    private String unique;

    public void setUnique(String unique) {
        this.unique = unique;
    }
    public String getUnique() {
        return unique;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    public String getBirthday() {
        return birthday;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }
    public String getHireDate() {
        return hireDate;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getGender() {
        return gender;
    }

    public void setIsHidePhone(String isHidePhone) {
        this.isHidePhone = isHidePhone;
    }
    public String getIsHidePhone() {
        return isHidePhone;
    }

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

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getUid() {
        return uid;
    }

    public void setPositiveDate(String positiveDate) {
        this.positiveDate = positiveDate;
    }
    public String getPositiveDate() {
        return positiveDate;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getPhone() {
        return phone;
    }

    public void setOrgUserType(String orgUserType) {
        this.orgUserType = orgUserType;
    }
    public String getOrgUserType() {
        return orgUserType;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
    public String getContact() {
        return contact;
    }

    public void setJobNo(String jobNo) {
        this.jobNo = jobNo;
    }
    public String getJobNo() {
        return jobNo;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
    public String getDepartment() {
        return department;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
}
