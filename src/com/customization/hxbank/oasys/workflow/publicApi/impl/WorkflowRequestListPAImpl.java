package com.customization.hxbank.oasys.workflow.publicApi.impl;

import com.customization.hxbank.oasys.workflow.entity.ApiWorkflowRequestInfo;
import com.customization.hxbank.oasys.workflow.publicApi.WorkflowPA;
import com.customization.hxbank.oasys.workflow.publicApi.WorkflowRequestTodoPA;
import com.engine.core.impl.Service;
import weaver.hrm.User;

import java.util.List;
import java.util.Map;


public class WorkflowRequestListPAImpl extends Service implements WorkflowRequestTodoPA {

    private WorkflowPA workflowPA = new WorkflowPAImpl();


    @Override
    public List<ApiWorkflowRequestInfo> getToDoRequestList(User user, String tabIds, Map<String, String> conditions, int pageNo, int pageSize, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestList(user,tabIds,conditions,pageNo,pageSize,isMergeShow,isNeedOs);
    }

    @Override
    public long getToDoRequestCount(User user, String tabIds, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestCount(user,tabIds,conditions,isMergeShow,isNeedOs);
    }

    // 获取指定user被退回的流程数据列表(只统计还在待办的)  待办退回 tabid为7
    @Override
    public List<ApiWorkflowRequestInfo> getBeRejectWorkflowRequestList(int pageNo, int pageSize, User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestList(user, "7", conditions, pageNo, pageSize, isMergeShow, isNeedOs);
    }

    // 获取指定user被退回的流程数据数量(只统计还在待办的)  待办退回 tabid为7
    @Override
    public long getBeRejectWorkflowReqeustCount(User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestCount(user,"7",conditions,isMergeShow,isNeedOs);
    }

    // 获取抄送给指定user的流程数据列表(只统计还在待办的) 待办抄送 tabid 为 9
    @Override
    public List<ApiWorkflowRequestInfo> getCCWorkflowRequestList(int pageNo, int pageSize, User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestList(user,"9",conditions,pageNo,pageSize,isMergeShow,isNeedOs);
    }

    // 获取抄送给指定user的流程数据数量(只统计还在待办的) 待办抄送 tabid 为 9
    @Override
    public long getCCWorkflowRequestCount(User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestCount(user,"9",conditions,isMergeShow,isNeedOs);
    }

    // 获取指定user的所有待处理流程数据列表 tabid 5
    @Override
    public List<ApiWorkflowRequestInfo> getDoingWorkflowRequestList(int pageNo, int pageSize, User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestList(user,"5",conditions,pageNo,pageSize,isMergeShow,isNeedOs);
    }

    // 获取指定user的所有待处理流程数量 tabid 5
    @Override
    public long getDoingWorkflowRequestCount(User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestCount(user,"5",conditions,isMergeShow,isNeedOs);
    }

    // 获取转发给指定user的所有流程数据列表(只统计还在待办的) tabid 8
    @Override
    public List<ApiWorkflowRequestInfo> getForwardWorkflowRequestList(int pageNo, int pageSize, User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestList(user,"8",conditions,pageNo,pageSize,isMergeShow,isNeedOs);
    }

    // 获取转发给指定user的所有流程数据数量(只统计还在待办的) tabid 8
    @Override
    public long getForwardWorkflowRequestCount(User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestCount(user,"8",conditions,isMergeShow,isNeedOs);
    }

    // 获取指定user的所有待阅流程数据列表 tabid 6 待阅
    @Override
    public List<ApiWorkflowRequestInfo> getToBeReadWorkflowRequestList(int pageNo, int pageSize, User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestList(user,"6",conditions,pageNo,pageSize,isMergeShow,isNeedOs);
    }

    // 获取指定user的所有待阅流程数据数量 tabid 6 待阅
    @Override
    public long getToBeReadWorkflowRequestCount(User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestCount(user,"6",conditions,isMergeShow,isNeedOs);
    }

    // 获取指定user的所有待办流程数据列表 tabid 0
    @Override
    public List<ApiWorkflowRequestInfo> getToDoWorkflowRequestList(int pageNo, int pageSize, User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestList(user,"0",conditions,pageNo,pageSize,isMergeShow,isNeedOs);
    }

    // 获取指定user的所有待办流程数据数量 tabid 0
    @Override
    public long getToDoWorkflowRequestCount(User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestCount(user,"0",conditions,isMergeShow,isNeedOs);
    }

    // 获取指定user的所有流程数据列表 tabid 0 所有待办 tabid 10 所有已办
    @Override
    public List<ApiWorkflowRequestInfo> getAllWorkflowRequestList(int pageNo, int pageSize, User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        // 获取全部流程
        return workflowPA.getToDoRequestList(user, "0,10", conditions, pageNo, pageSize, isMergeShow, isNeedOs);
    }
    // 获取指定user的所有流程数据数量
    @Override
    public long getAllWorkflowRequestCount(User user, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        return workflowPA.getToDoRequestCount(user,"0,10",conditions,isMergeShow,isNeedOs);
    }

}
