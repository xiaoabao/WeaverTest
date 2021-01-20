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
 *工作流基本信息
 */
public class ApiWorkflowBaseInfo implements Serializable {
    // 工作流id
    private String workflowId;
    // 工作流 名称
    private String workflowName;
    // 工作流类型 id
    private String workflowTypeId;
    // 工作流类型 名称
    private String workflowTypeName;
    // 工作流 表单id
    private String formId;

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowTypeId() {
        return workflowTypeId;
    }

    public void setWorkflowTypeId(String workflowTypeId) {
        this.workflowTypeId = workflowTypeId;
    }

    public String getWorkflowTypeName() {
        return workflowTypeName;
    }

    public void setWorkflowTypeName(String workflowTypeName) {
        this.workflowTypeName = workflowTypeName;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }
}
