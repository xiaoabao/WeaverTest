package com.customization.hxbank.oasys.service;

import com.alibaba.fastjson.JSON;
import com.customization.hxbank.oasys.entity.PrtcToken;
import com.google.common.collect.Lists;
import com.weaver.qfengx.*;
import com.weaver.qfengx.entity.Result;
import org.apache.commons.fileupload.FileItem;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;

import java.util.List;
import java.util.Objects;

/**
 * 印控机相关服务类
 * Created by YeShengtao on 2020/9/24 16:54
 */
public class PrtcService extends BaseBean {

    public String handleFile(String requestid, String fieldname,
                             FileItem uploadItem, String token, String userid, String ip) {
        writeLog("======> 开始印控机文件上传");
        try {
            if (StringUtils.isEmpty(requestid) || Objects.isNull(uploadItem)
                    || StringUtils.isEmpty(fieldname) || StringUtils.isEmpty(token)
                    || StringUtils.isEmpty(userid)) {
                return JSON.toJSONString(Result.paramErr());
            }
            PrtcToken prtcToken = queryPrtcToken(token);
            // 检查token
            if (!checkPrtcToken(prtcToken)) {
                return JSON.toJSONString(Result.tokenErr());
            }

            // 查询配置的文档目录
            String dirId = queryDirId(requestid, prtcToken);
            if (StringUtils.isEmpty(dirId)) {
                writeLog("requestid => " + requestid + ", token => " + prtcToken.getToken() + " 对应的文档目录配置不存在");
                return JSON.toJSONString(Result.fail("requestid => " + requestid + ", token => " + prtcToken.getToken() + " 对应的文档目录配置不存在"));
            }
            // 将文件写入到文档中
            int docid = ImageFileUtils.uploadFile(uploadItem.getInputStream(), User.getUser(Util.getIntValue(userid), 0),
                    dirId, uploadItem.getName());
            // 将数据回写到流程字段中
            putDocIdToWorflowData(docid, requestid, fieldname);
            // 插入上传日志
            insertUploadLog(prtcToken.getId(), requestid, uploadItem.getName(), uploadItem.getSize(), userid,
                    dirId, fieldname, ip, Integer.toString(docid));
            return JSON.toJSONString(Result.ok());
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.toString()));
        } finally {
            writeLog("======> 完成印控机文件上传");
        }
    }

    public void insertUploadLog(String token, String requestid, String filename, long fileSize, String user,
                                String dir, String fieldname, String ip, String docid) {
        List<String> param = Lists.newArrayList(
                "token", "workflow", "qqid", "uploadTime", "filename",
                "filesize", "uploadUser", "dir", "fieldname", "frtcIp", "docid"
        );
        List<Object> data = Lists.newArrayList(
                token, WorkflowUtils.requestIdToworkflowId(requestid), requestid, DateUtils.date(),
                filename, fileSize, user, dir, fieldname, ip, docid
        );
        DaoUtils.insert("uf_prtc_log", param, data);
    }

    public void putDocIdToWorflowData(int docid, String requestid, String fieldname) {
        String maintable = WorkflowUtils.mainTable(WorkflowUtils.requestIdToworkflowId(requestid));
        // 获取原有值
        String oldVal = DaoUtils.querySingleVal(String.format("select %s from %s where requestid = ?", fieldname, maintable), requestid);
        // 写入新值
        DaoUtils.executeUpdate(String.format("update %s set %s = ? where requestid = ?", maintable, fieldname), oldVal + "," + docid, requestid);
    }

    /**
     * 根据requestid 文档配置表中的文档配置
     */
    public String queryDirId(String requestid, PrtcToken token) {
        String workflowid = WorkflowUtils.requestIdToworkflowId(requestid);
        return DaoUtils.querySingleVal("select dir from uf_prtc_dir where workflow = ? and token = ?", workflowid, token.getId());
    }

    /**
     * 检查印控机Token是否有效
     */
    public boolean checkPrtcToken(PrtcToken prtcToken) {
        if (Objects.isNull(prtcToken) || StringUtils.equals(prtcToken.getStatus(), "")) {
            return false;
        }
        return true;
    }

    public PrtcToken queryPrtcToken(String token) {
        return DaoUtils.findSqlToBean(PrtcToken.class, "select id, token, remark, status from uf_prtc_token where token = ?", token);
    }

}
