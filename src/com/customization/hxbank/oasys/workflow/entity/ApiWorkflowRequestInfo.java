package com.customization.hxbank.oasys.workflow.entity;

/**
 * @Description
 * @Author miao.zhang <yyem954135@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2020-10-10
 */
import java.io.Serializable;

/**
 * 流程列表 基本数据 我的请求，待办，已办
 */
public class ApiWorkflowRequestInfo implements Serializable {
    // 请求id
    private String requestId;
    // 请求标题
    private String requestName;
    // 请求重要级别
    private String requestLevel;
    // 短信提醒
    private String messageType;
    // 工作流基本 信息
    private ApiWorkflowBaseInfo workflowBaseInfo;
    // 当前节点id
    private String currentNodeId;
    // 当前节点名称
    private String currentNodeName;
    // 路程状态
    private String status;
    // 创建人 id
    private String creatorId;
    // 创建人姓名
    private String creatorName;
    // 创建时间
    private String createTime;
    // 最后操作者id
    private String lastOperatorId;
    // 最后操作者名称
    private String lastOperatorName;
    // 最后操作时间
    private String lastOperateTime;
    // 已读状态
    private String viewtype;
    // pcurl
    private String pcurl;
    // 接受时间
    private String receiveTime;
    // 系统名称
    private String sysName;

    private String showTime;

    @Override
    public String toString() {
        return "ApiWorkflowRequestInfo{" +
                "requestId='" + requestId + '\'' +
                ", requestName='" + requestName + '\'' +
                ", requestLevel='" + requestLevel + '\'' +
                ", messageType='" + messageType + '\'' +
                ", workflowBaseInfo=" + workflowBaseInfo +
                ", currentNodeId='" + currentNodeId + '\'' +
                ", currentNodeName='" + currentNodeName + '\'' +
                ", status='" + status + '\'' +
                ", creatorId='" + creatorId + '\'' +
                ", creatorName='" + creatorName + '\'' +
                ", createTime='" + createTime + '\'' +
                ", lastOperatorId='" + lastOperatorId + '\'' +
                ", lastOperatorName='" + lastOperatorName + '\'' +
                ", lastOperateTime='" + lastOperateTime + '\'' +
                ", viewtype='" + viewtype + '\'' +
                ", pcurl='" + pcurl + '\'' +
                ", receiveTime='" + receiveTime + '\'' +
                ", sysName='" + sysName + '\'' +
                ", showTime='" + showTime + '\'' +
                '}';
    }

    public String getShowTime() {
        return showTime;
    }

    public ApiWorkflowRequestInfo setShowTime(String showTime) {
        this.showTime = showTime;
        return this;
    }

    public String getPcurl() {
        return pcurl;
    }
    public void setPcurl(String pcurl) {
        this.pcurl = pcurl;
    }
    public String getViewtype() {
        return viewtype;
    }

    public void setViewtype(String viewtype) {
        this.viewtype = viewtype;
    }

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public String getRequestLevel() {
        return requestLevel;
    }

    public void setRequestLevel(String requestLevel) {
        this.requestLevel = requestLevel;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public ApiWorkflowBaseInfo getWorkflowBaseInfo() {
        return workflowBaseInfo;
    }

    public void setWorkflowBaseInfo(ApiWorkflowBaseInfo workflowBaseInfo) {
        this.workflowBaseInfo = workflowBaseInfo;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public String getCurrentNodeName() {
        return currentNodeName;
    }

    public void setCurrentNodeName(String currentNodeName) {
        this.currentNodeName = currentNodeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastOperatorId() {
        return lastOperatorId;
    }

    public void setLastOperatorId(String lastOperatorId) {
        this.lastOperatorId = lastOperatorId;
    }

    public String getLastOperatorName() {
        return lastOperatorName;
    }

    public void setLastOperatorName(String lastOperatorName) {
        this.lastOperatorName = lastOperatorName;
    }

    public String getLastOperateTime() {
        return lastOperateTime;
    }

    public void setLastOperateTime(String lastOperateTime) {
        this.lastOperateTime = lastOperateTime;
    }

    public String getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }
}

