package com.api;

import com.alibaba.fastjson.JSON;
import com.api.doc.detail.util.ImageConvertUtil;
import com.customization.hxbank.oasys.entity.ParamToken;
import com.customization.hxbank.oasys.service.ParamService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.weaver.qfengx.*;
import com.weaver.qfengx.entity.Result;
import weaver.conn.RecordSet;
import weaver.general.ImageUtil;
import weaver.general.Util;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.hrm.UserManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * 参数查询API
 * Created by YeShengtao on 2020/10/20 17:10
 */
@Path("seconddev/hxbank/param")
public class OaParamApi {

    private ParamService paramService = new ParamService();

    private static final LogUtils log = new LogUtils(OaParamApi.class);


    public User getUser(String loginid) {
        Map<String, String> userMap = DaoUtils.executeQueryToMap("select " +
                "id,password,lastname,sex,telephone,mobile,mobilecall,email,countryid,locationid," +
                "resourcetype,startdate,enddate,jobtitle,joblevel,seclevel,departmentid,subcompanyid1, " +
                "managerid,assistantid  " +
                "from hrmresource where loginid = ?", loginid);
        UserManager userManager = new UserManager();
        return userManager.getUserByUserIdAndLoginType(NumberUtils.parseInt(StringUtils.val(userMap.get("id"))), "1");
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String list(@Context HttpServletRequest request,
                       @Context HttpServletResponse response) {


        try {
            User user = HrmUserVarify.getUser(request, response);
            log.writeLog(getUser("test1"));
            log.writeLog(JSON.toJSON(user));
            log.writeLog(JSON.toJSON(request.getSession(true).getAttribute("weaver_user@bean")));
            // 查询邮件列表
            // 配置的帮助中心邮件ID
            String ids = PropUtils.get("HxbankMailHelp", "mailids");
            List<Map<String, String>> mapList = DaoUtils.executeQueryToMapList(String.format("select " +
                    "mailid, id, filesize, filename  " +
                    "from  mailresourcefile  " +
                    "where mailid in (%s) and isfileattrachment = ? order by id asc", ids), "1");
            List<Map<String, Object>> dataList = Lists.newArrayList();
            mapList.forEach(x -> {
                Map<String, Object> resMap = Maps.newHashMap();
                String filename = StringUtils.val(x.get("filename"));
                int lastIndex = filename.lastIndexOf(".");
                String extName = lastIndex != -1 ? filename.substring(lastIndex) : "";
                Map<String, String> iconMap = getfileTypeIcon(extName);
                String mailid = StringUtils.val(x.get("mailid"));
                String fileid = StringUtils.val(x.get("id"));
                resMap.put("mailId", mailid);
                resMap.put("fileid", fileid);
                resMap.put("fileExtendName", extName);
                resMap.put("id", fileid);
                resMap.put("filelink", getDownloadUrl(mailid, fileid));
                resMap.put("filename", filename);
                resMap.put("filesize", getFileSize(StringUtils.val(x.get("filesize"))));
                resMap.put("filesizeByte", Long.parseLong(StringUtils.val(x.get("filesize"))));
                resMap.put("imgSrc", "");
                resMap.put("loadlink", "");
                resMap.put("showLoad", "true");
                resMap.put("showDelete", "true");
                resMap.put("icon", iconMap.get("icon"));
                resMap.put("iconColor", iconMap.get("iconColor"));
                String ddcode = user.getUID() + "_" + fileid;
                try {
                    weaver.docs.docs.util.DesUtils des = new weaver.docs.docs.util.DesUtils();
                    ddcode = des.encrypt(ddcode);
                } catch (Exception e1) {
                }
                resMap.put("ddcode", ddcode);
                boolean isImg = isImageFile(extName);
                resMap.put("isImg", String.valueOf(isImg)); //是否是图片
                resMap.put("previewUrl", getPreviewUrl(String.valueOf(mailid), fileid)); //预览地址链接
                resMap.put("previewUrlNew", getNewPreviewUrl(Util.getIntValue(fileid))); //预览地址链接(新版)
                //手机端预览问题
                boolean isCanPreviewForMobile = false; //该文件是否支持手机端预览
                try {
                    ImageConvertUtil imageConvertUtil = new ImageConvertUtil();
                    //可预览 ： 是图片 或者  （开启了独立服务转换器  且  是可预览的文件类型）。
                    isCanPreviewForMobile = isImg || (imageConvertUtil.convertForClient() && ImageConvertUtil.canConvertType(extName.replaceFirst(".", "")));
                } catch (Exception e) {
                }
//                resMap.put("isCanPreviewForMobile", String.valueOf(isCanPreviewForMobile));
                resMap.put("isCanPreviewForMobile", "true");
                dataList.add(resMap);
            });
            return JSON.toJSONString(Result.ok(dataList));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    public String getPreviewUrl(String mailid, String mailFileId) {
        if (Util.null2String(mailid).isEmpty()) {
            mailid = getMailIdByMailFileId(mailFileId);
        }
        //download=0 或者为空 或者不传此参数
        if ("0".equals(mailid)) {
            String mrf_uuid = "";
            RecordSet rs = new RecordSet();
            rs.executeQuery("select mrf_uuid from mailresourcefile where id = ?", mailFileId);
            while (rs.next()) {
                mrf_uuid = rs.getString("mrf_uuid");
            }
            return "/weaver/weaver.email.FileDownloadLocation?fileid=" + mailFileId + "&mailid=" + mailid + "&mrfuuid=" + mrf_uuid;
        } else {
            return "/weaver/weaver.email.FileDownloadLocation?fileid=" + mailFileId + "&mailid=" + mailid;
        }
    }

    /**
     * 邮件附件预览地址。
     *
     *
     * @param mailFileId 邮件附件id
     * @return
     */
    public static String getNewPreviewUrl(int mailFileId) {
        return "/docs/view/imageFileView.jsp?fileid=" + mailFileId;
    }

    /**
     * 判断扩展名是不是图片
     *
     * @param extName
     * @return true：是图片，false：不是
     */
    public static boolean isImageFile(String extName) {
        extName = Util.null2String(extName).replaceFirst(".", "").toLowerCase();
        Matcher matcher = ImageUtil.IMAGE_PATTERN.matcher(extName);
        return matcher.find();
    }

    /**
     * 根据文件字节数获得大小字符串（用于页面展示）
     *
     * @param fileSize 文件字节大小
     * @return 文件大小展示用字符串
     */
    public static String getFileSize(String fileSize) {
        float filesize = Util.getFloatValue(fileSize, 0f);
        if (filesize < 0) {
            filesize = 0;
        }
        String result = "";
        if (filesize < 511) {
            result = filesize + "B"; // 字节
        } else if (filesize < 1024 * 1024) {
            result = String.format("%.2fKB", filesize * 1.0F / (1024));
        } else if (filesize < 1024 * 1024 * 1024) {
            result = String.format("%.2fMB", filesize * 1.0F / (1024 * 1024));
        } else {
            result = String.format("%.2fG", filesize * 1.0F / (1024 * 1024 * 1024));
        }
        return result;
    }

    public String getDownloadUrl(String mailid, String mailFileId) {
        if (Util.null2String(mailid).isEmpty()) {
            mailid = getMailIdByMailFileId(mailFileId);
        }
        if ("0".equals(mailid)) {
            String mrf_uuid = "";
            RecordSet rs = new RecordSet();
            rs.executeQuery("select mrf_uuid from mailresourcefile where id = ?", mailFileId);
            while (rs.next()) {
                mrf_uuid = rs.getString("mrf_uuid");
            }
            return "/weaver/weaver.email.FileDownloadLocation?download=1&fileid=" + mailFileId + "&mailid=" + mailid + "&mrfuuid=" + mrf_uuid;
        } else {
            return "/weaver/weaver.email.FileDownloadLocation?download=1&fileid=" + mailFileId + "&mailid=" + mailid;
        }
    }

    private String getMailIdByMailFileId(String mailFileId) {
        String mailId = "0";
        if ("".equals(Util.null2String(mailFileId))) {
            return mailId;
        }
        try {
            RecordSet rs = new RecordSet();
            String sql = "select mailid from mailresourcefile where id = ?";
            rs.executeQuery(sql, mailFileId);
            if (rs.next()) {
                mailId = Util.null2o(rs.getString("mailid"));
            }
        } catch (Exception e) {
            log.writeLog("获取邮件id失败,mailFileId=" + mailFileId);
            log.writeLog(e);
        }
        return mailId;
    }

    /**
     * 获取附件类型图标
     */
    public Map<String, String> getfileTypeIcon(String fileType) {
        Map<String, String> iconMap = new HashMap<>();
        String icon = "file";
        String iconColor = "#009EFB";
        fileType = fileType.toLowerCase();
        switch (fileType) {
            case ".docx":
            case ".doc":
            case ".txt":
            case ".wps":
            case ".md":
                icon = "word";
                iconColor = "#009EFB";
                break;
            case ".gif":
            case ".png":
            case ".jpeg":
            case ".jpg":
            case ".bmp":
            case ".svg":
                icon = "pic";
                iconColor = "#009EFB";
                break;
            case ".pdf":
                icon = "pdf";
                iconColor = "#DF4430";
                break;
            case ".xls":
            case ".xlsx":
                icon = "excel";
                iconColor = "#64D16F";
                break;
            case ".zip":
            case ".rar":
            case ".gz":
            case ".z":
            case ".7z":
                icon = "rar";
                iconColor = "#716BFF";
                break;
            case ".html":
            case ".jsp":
            case ".js":
            case ".css":
            case ".htm":
            case ".php":
                icon = "html";
                iconColor = "#FFBB32";
                break;
            default:
                icon = "file";
                iconColor = "#009EFB";
                break;
        }
        iconMap.put("icon", icon);
        iconMap.put("iconColor", iconColor);
        return iconMap;
    }

    /**
     * 查询当前用户信息
     */
    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public String user(@Context HttpServletRequest request,
                       @Context HttpServletResponse response) {
        try {
            User user = HrmUserVarify.getUser(request, response);
            Map<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("name", user.getUsername());
            dataMap.put("loginip", user.getLoginip());
            dataMap.put("telephone", user.getTelephone());
            dataMap.put("deptId", user.getUserDepartment());
            dataMap.put("department", DaoUtils.querySingleVal("select departmentname from HrmDepartment where id = ?", user.getUserDepartment()));
            dataMap.put("compId", user.getUserSubCompany1());
            dataMap.put("company", DaoUtils.querySingleVal("select subcompanyname from HrmSubCompany where id = ?", user.getUserSubCompany1()));
            return JSON.toJSONString(Result.ok(dataMap));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    /**
     * 用户ID查询
     */
    @GET
    @Path("/hrm/user/id")
    @Produces(MediaType.APPLICATION_JSON)
    public String hrmUserId(@Context HttpServletRequest request,
                                    @Context HttpServletResponse response) {
        try {
            String loginid = request.getParameter("loginid");
            String token = request.getParameter("token");
            if (StringUtils.isEmpty(loginid) || StringUtils.isEmpty(token)) {
                return JSON.toJSONString(Result.paramErr());
            }
            // 校验token
            if (!paramService.checkParamToken(token)) {
                return JSON.toJSONString(Result.tokenErr());
            }
            ParamToken paramToken = paramService.queryToken(token);
            if (!paramService.checkParamTokenIp(paramToken, request)) {
                return JSON.toJSONString(Result.fail("IP：" + RequestUtils.ip(request) + " 不合法"));
            }
            String id = DaoUtils.querySingleVal("select id from hrmresource where loginid = ?", loginid);
            if (StringUtils.isEmpty(id)) {
                return JSON.toJSONString(Result.fail("人员不存在"));
            }
            return JSON.toJSONString(Result.ok(id));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    /**
     * 部门ID查询
     */
    @GET
    @Path("/hrm/dept/id")
    @Produces(MediaType.APPLICATION_JSON)
    public String hrmDeptId(@Context HttpServletRequest request,
                                    @Context HttpServletResponse response) {
        try {
            String outkey = request.getParameter("outkey");
            String token = request.getParameter("token");
            if (StringUtils.isEmpty(outkey) || StringUtils.isEmpty(token)) {
                return JSON.toJSONString(Result.paramErr());
            }
            // 校验token
            if (!paramService.checkParamToken(token)) {
                return JSON.toJSONString(Result.tokenErr());
            }
            ParamToken paramToken = paramService.queryToken(token);
            if (!paramService.checkParamTokenIp(paramToken, request)) {
                return JSON.toJSONString(Result.fail("IP：" + RequestUtils.ip(request) + " 不合法"));
            }
            String id = DaoUtils.querySingleVal("select id from hrmdepartment where outkey = ?", outkey);
            if (StringUtils.isEmpty(id)) {
                return JSON.toJSONString(Result.fail("部门不存在"));
            }
            return JSON.toJSONString(Result.ok(id));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    /**
     * 分部ID查询
     */
    @GET
    @Path("/hrm/subcom/id")
    @Produces(MediaType.APPLICATION_JSON)
    public String hrmSubcomId(@Context HttpServletRequest request,
                            @Context HttpServletResponse response) {
        try {
            String outkey = request.getParameter("outkey");
            String token = request.getParameter("token");
            if (StringUtils.isEmpty(outkey) || StringUtils.isEmpty(token)) {
                return JSON.toJSONString(Result.paramErr());
            }
            // 校验token
            if (!paramService.checkParamToken(token)) {
                return JSON.toJSONString(Result.tokenErr());
            }
            ParamToken paramToken = paramService.queryToken(token);
            if (!paramService.checkParamTokenIp(paramToken, request)) {
                return JSON.toJSONString(Result.fail("IP：" + RequestUtils.ip(request) + " 不合法"));
            }
            String id = DaoUtils.querySingleVal("select id from hrmsubcompany where outkey = ?", outkey);
            if (StringUtils.isEmpty(id)) {
                return JSON.toJSONString(Result.fail("分部不存在"));
            }
            return JSON.toJSONString(Result.ok(id));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    /**
     * 分部部门混合查询
     */
    @GET
    @Path("/hrm/subcomAndDept/id")
    @Produces(MediaType.APPLICATION_JSON)
    public String hrmSubcomAndDeptId(@Context HttpServletRequest request,
                              @Context HttpServletResponse response) {
        try {
            String outkey = request.getParameter("outkey");
            String token = request.getParameter("token");
            if (StringUtils.isEmpty(outkey) || StringUtils.isEmpty(token)) {
                return JSON.toJSONString(Result.paramErr());
            }
            // 校验token
            if (!paramService.checkParamToken(token)) {
                return JSON.toJSONString(Result.tokenErr());
            }
            ParamToken paramToken = paramService.queryToken(token);
            if (!paramService.checkParamTokenIp(paramToken, request)) {
                return JSON.toJSONString(Result.fail("IP：" + RequestUtils.ip(request) + " 不合法"));
            }
            String comId = DaoUtils.querySingleVal("select id from hrmsubcompany where outkey = ?", outkey);
            String deptId = DaoUtils.querySingleVal("select id from hrmdepartment where outkey = ?", outkey);
            Map<String, String> resMap = Maps.newHashMap();
            resMap.put("com", comId);
            resMap.put("dept", deptId);
            return JSON.toJSONString(Result.ok(resMap));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }
}
