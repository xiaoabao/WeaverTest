package com.customization.hxbank.oasys.workflow.publicApi;

import com.customization.hxbank.oasys.workflow.entity.ApiWorkflowRequestInfo;
import weaver.hrm.User;

import java.util.List;
import java.util.Map;

/**
 * Created by wcc on 2019/10/31.
 */
public interface WorkflowRequestTodoPA {

    public List<ApiWorkflowRequestInfo> getToDoRequestList(User user, String tabIds, Map <String, String> conditions, int pageNo, int pageSize, boolean isMergeShow, boolean isNeedOs);

    public long getToDoRequestCount(User user, String tabIds, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 待办 中退回的流程 列表
    public List<ApiWorkflowRequestInfo> getBeRejectWorkflowRequestList(int pageNo, int pageSize, User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 待办 中退回的流程 数量
    public long getBeRejectWorkflowReqeustCount(User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 待办 中抄送的流程 列表
    public List<ApiWorkflowRequestInfo> getCCWorkflowRequestList(int pageNo, int pageSize, User userid, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    //待办 中抄送的流程 数量
    public long getCCWorkflowRequestCount(User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 待处理 流程 列表
    public List<ApiWorkflowRequestInfo> getDoingWorkflowRequestList(int pageNo, int pageSize, User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 待处理 流程 数量
    public long getDoingWorkflowRequestCount(User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 获取转发给指定user的所有流程数据列表(只统计还在待办的)
    public List<ApiWorkflowRequestInfo> getForwardWorkflowRequestList(int pageNo, int pageSize, User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 获取转发给指定user的所有流程数据数量(只统计还在待办的)
    public long getForwardWorkflowRequestCount(User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 获取指定user的所有待阅流程数据列表
    public List<ApiWorkflowRequestInfo> getToBeReadWorkflowRequestList(int pageNo, int pageSize, User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 获取指定user的所有待阅流程数据数量
    public long getToBeReadWorkflowRequestCount(User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 获取指定user的所有待办流程数据列表
    public List<ApiWorkflowRequestInfo> getToDoWorkflowRequestList(int pageNo, int pageSize, User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 获取指定user的所有待办流程数据数量
    public long getToDoWorkflowRequestCount(User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 获取指定user的所有流程数据列表
    public List<ApiWorkflowRequestInfo> getAllWorkflowRequestList(int pageNo, int pageSize, User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

    // 获取指定user的所有流程数量
    public long getAllWorkflowRequestCount(User user, Map <String, String> conditions, boolean isMergeShow, boolean isNeedOs);

}
