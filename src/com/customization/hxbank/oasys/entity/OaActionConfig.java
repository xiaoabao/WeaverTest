package com.customization.hxbank.oasys.entity;

import com.weaver.qfengx.annotation.DaoField;

import java.util.List;

/**
 * Created by YeShengtao on 2020/12/2 9:50
 */
public class OaActionConfig {

    private String id;
    private String workflowids;
    private String isDebug;
    private String workflowType;
    @DaoField(false)
    private List<OaActionConfigDetail> detailList;

    public List<OaActionConfigDetail> getDetailList() {
        return detailList;
    }

    public OaActionConfig setDetailList(List<OaActionConfigDetail> detailList) {
        this.detailList = detailList;
        return this;
    }

    public String getId() {
        return id;
    }

    public OaActionConfig setId(String id) {
        this.id = id;
        return this;
    }

    public String getWorkflowids() {
        return workflowids;
    }

    public OaActionConfig setWorkflowids(String workflowids) {
        this.workflowids = workflowids;
        return this;
    }

    public String getIsDebug() {
        return isDebug;
    }

    public OaActionConfig setIsDebug(String isDebug) {
        this.isDebug = isDebug;
        return this;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public OaActionConfig setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
        return this;
    }
}
