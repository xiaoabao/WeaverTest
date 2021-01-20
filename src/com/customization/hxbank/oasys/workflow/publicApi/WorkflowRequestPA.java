package com.customization.hxbank.oasys.workflow.publicApi;

import weaver.hrm.User;

import java.util.Map;

/**
 * Created by wcc on 2019/11/1.
 */
public interface WorkflowRequestPA {

    public Object getWorkflowRequest(User user, int requestid, Map <String, Object> otherParam);

    /**
     * 获取流程相关资源
     * @param user
     * @param requestid
     * @param otherParams
     * @return
     */
    public Object getRequestResources(User user, int requestid, Map <String, Object> otherParams);
    /**
     *
     * @param user
     * @param requestid
     * @param otherParam
     * @return
     */
    public Object getRequestStatus(User user, int requestid, Map <String, Object> otherParam);
}
