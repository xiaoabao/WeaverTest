package com.api;

import com.alibaba.fastjson.JSON;
import com.customization.hxbank.oasys.entity.ParamToken;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.weaver.qfengx.*;
import com.weaver.qfengx.entity.Result;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import weaver.general.Util;
import weaver.hrm.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Objects;

/**
 * 文档上传接口
 * Created by YeShengtao on 2020/11/23 18:05
 */
@Path("seconddev/hxbank/doc")
public class OaDocApi {

    public static final LogUtils log = new LogUtils(OaDocApi.class);

    @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    public String upload(@Context HttpServletRequest request,
                         @Context HttpServletResponse response) {
        try {
            log.writeLog("======> 处理接口 ===> 文档上传");
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);


            List<FileItem> files = upload.parseRequest(request);
            String dirId = null;
            String userid = null;
            String token = null;
            FileItem uploadItem = null;
            for (FileItem fileItem : files) {
                switch (fileItem.getFieldName()) {
                    case "file":
                        uploadItem = fileItem;
                        break;
                    case "dirId":
                        dirId = IOUtils.string(fileItem.getInputStream());
                        break;
                    case "user":
                        userid = IOUtils.string(fileItem.getInputStream());
                        break;
                    case "token":
                        token = IOUtils.string(fileItem.getInputStream());
                        break;
                }
            }
            // 校验token
            if (!checkParamToken(token)) {
                return JSON.toJSONString(Result.tokenErr());
            }
            ParamToken paramToken = queryToken(token);
            if (!checkParamTokenIp(paramToken, request)) {
                return JSON.toJSONString(Result.fail("IP：" + RequestUtils.ip(request) + " 不合法"));
            }
            log.writeLog(String.format("dirId => %s, token => %s, userid => %s", dirId, token, userid));
            return handleFile(uploadItem, userid, dirId, uploadItem.getName());
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.toString()));
        }
    }

    @POST
    @Path("/uploadforoa")
    @Produces(MediaType.APPLICATION_JSON)
    public String uploadforoa(@Context HttpServletRequest request,
                              @Context HttpServletResponse response) {
        try {
            log.writeLog("======> 处理接口 ===> 文档上传");
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> files = upload.parseRequest(request);
            String categroyid = "";
            String userid = "";
            String name = "";
            String token = "";
            FileItem uploadItem = null;
            for (FileItem fileItem : files) {
                switch (fileItem.getFieldName()) {
                    case "filename":
                        uploadItem = fileItem;
                        break;
                    case "category":
                        categroyid = IOUtils.string(fileItem.getInputStream());
                        break;
                    case "name":
                        name = IOUtils.string(fileItem.getInputStream());
                        break;
                    case "token":
                        token = IOUtils.string(fileItem.getInputStream());
                        break;
                    case "userid":
                        userid = IOUtils.string(fileItem.getInputStream());
                        break;
                }
            }

            if (StringUtils.isEmpty(token)) {
                return JSON.toJSONString(Result.paramErr());
            }
// 校验token
            if (!checkParamToken(token)) {
                return JSON.toJSONString(Result.tokenErr());
            }
            ParamToken paramToken = queryToken(token);
            if (!checkParamTokenIp(paramToken, request)) {
                return JSON.toJSONString(Result.fail("IP：" + RequestUtils.ip(request) + " 不合法"));
            }
            log.writeLog(String.format("name => %s, token => %s, userid => %s, categroyid => ", name, token, userid, categroyid));
            return handleFile(uploadItem, userid, categroyid, name);
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.toString()));
        }
    }

    @POST
    @Path("/uploadforoa")
    @Produces(MediaType.APPLICATION_JSON)
    public String hrmUserId(@Context HttpServletRequest request,
                            @Context HttpServletResponse response) {
        try {
            log.writeLog("======> 处理接口 ===> 文档上传");
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> files = upload.parseRequest(request);
            String categroyid = "";
            String userid = "";
            String filename = "";
            String token = "";
            FileItem uploadItem = null;
            for (FileItem fileItem : files) {
                switch (fileItem.getFieldName()) {
                    case "filename":
                        uploadItem = fileItem;
                        break;
                    case "category":
                        categroyid = IOUtils.string(fileItem.getInputStream());
                        break;
                    case "name":
                        filename = IOUtils.string(fileItem.getInputStream());
                        break;
                    case "token":
                        token = IOUtils.string(fileItem.getInputStream());
                        break;
                    case "userid":
                        userid = IOUtils.string(fileItem.getInputStream());
                        break;
                }
            }

            if (StringUtils.isEmpty(token)) {
                return JSON.toJSONString(Result.paramErr());
            }
// 校验token
            if (!checkParamToken(token)) {
                return JSON.toJSONString(Result.tokenErr());
            }
            ParamToken paramToken = queryToken(token);
            if (!checkParamTokenIp(paramToken, request)) {
                return JSON.toJSONString(Result.fail("IP：" + RequestUtils.ip(request) + " 不合法"));
            }

            return handleFile(uploadItem, userid, categroyid, filename);
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.toString()));
        }
    }

    private String handleFile(FileItem uploadItem, String userid, String categroyid, String filename) {
        try {

            // 将文件写入到文档中
            int docid = ImageFileUtils.uploadFile(uploadItem.getInputStream(), User.getUser(Util.getIntValue(userid), 0),
                    categroyid, filename);
            DaoUtils.executeUpdate("update docdetail set doctype = 2 where id = ?", docid);
            DaoUtils.executeUpdate("update docimagefile set isextfile = ? where docid = ?", "", docid);
            return JSON.toJSONString(Result.ok(docid));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.toString()));
        } finally {
            log.writeLog("======> 完成文件上传");
        }
    }

    public static ParamToken queryToken(String token) {
        return DaoUtils.findSqlToBean(ParamToken.class, "select * from uf_param_token where token = ?", token);
    }

    /**
     * 检查数据归档Token是否有效
     */
    public static boolean checkParamToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }

        ParamToken paramToken = DaoUtils.findSqlToBean(ParamToken.class, "select * from uf_param_token where token = ? and status = 0", token);
        if (Objects.isNull(paramToken) || StringUtils.equals(paramToken.getStatus(), "")) {
            return false;
        }
        return true;
    }

    /**
     * 检查Token的IP是否合法
     */
    public static boolean checkParamTokenIp(ParamToken token, HttpServletRequest request) {
        String ips = token.getIp();
        if (StringUtils.isBlank(ips)) {
            return true;
        }
        List<String> ipList = Lists.newArrayList(Splitter.on(",").trimResults().omitEmptyStrings().split(ips));
        if (ipList.contains(RequestUtils.ip(request))) {
            return true;
        }
        return false;
    }

}
