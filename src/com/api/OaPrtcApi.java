package com.api;

import com.alibaba.fastjson.JSON;
import com.customization.hxbank.oasys.service.PrtcService;
import com.weaver.qfengx.IDUtils;
import com.weaver.qfengx.IOUtils;
import com.weaver.qfengx.RequestUtils;
import com.weaver.qfengx.entity.Result;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import weaver.general.BaseBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * 印控机接口API
 * Created by YeShengtao on 2020/9/25 15:33
 */
@Path("seconddev/hxbank/prtc")
public class OaPrtcApi extends BaseBean {

    private PrtcService prtControllerService = new PrtcService();

    /**
     * 印控机标识生成
     */
    @GET
    @Path("/id")
    @Produces(MediaType.APPLICATION_JSON)
    public String typeId(@Context HttpServletRequest request,
                         @Context HttpServletResponse response) {
        try {
            // 根据雪花算法 生成ID
            return JSON.toJSONString(Result.ok("PT" + IDUtils.snowFlakeId()));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    public String upload(@Context HttpServletRequest request,
                         @Context HttpServletResponse response) {
        try {
            writeLog("======> 处理接口 ===> 印控机附件上传");
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> files = upload.parseRequest(request);
            String requestid = null;
            String fieldname = null;
            String token = null;
            String userid = null;
            FileItem uploadItem = null;
            for (FileItem fileItem : files) {
                switch (fileItem.getFieldName()) {
                    case "requestid":
                        requestid = IOUtils.string(fileItem.getInputStream()); break;
                    case "file":
                        uploadItem = fileItem; break;
                    case "fieldname":
                        fieldname = IOUtils.string(fileItem.getInputStream()); break;
                    case "token":
                        token = IOUtils.string(fileItem.getInputStream()); break;
                    case "user":
                        userid = IOUtils.string(fileItem.getInputStream()); break;
                }
            }
           return prtControllerService.handleFile(requestid, fieldname, uploadItem, token, userid, RequestUtils.ip(request));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.toString()));
        }
    }

}
