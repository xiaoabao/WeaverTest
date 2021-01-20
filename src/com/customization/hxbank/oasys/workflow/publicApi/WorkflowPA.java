package com.customization.hxbank.oasys.workflow.publicApi;


import com.customization.hxbank.oasys.workflow.entity.ApiWorkflowRequestInfo;

import weaver.hrm.User;

import java.util.List;
import java.util.Map;

public interface WorkflowPA {
    // 根据 tabids 获取流程列表
    public List <ApiWorkflowRequestInfo> getRequestList(User user, String tabIds, Map <String, String> conditions, int pageNo, int pageSize, boolean isMergeShow, boolean isNeedOs);
    // 根据 tabids 获取流程数量
    public long getRequestCount(User user, String tabIds, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs);
    // 根据 tabids 获取 流程列表
    public List <ApiWorkflowRequestInfo> getToDoRequestList(User user, String tabIds, Map<String, String> conditions, int pageNo, int pageSize, boolean isMergeShow, boolean isNeedOs);
    // 根据 tabids 获取 流程数量
    public long getToDoRequestCount(User user, String tabIds, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs);


}
