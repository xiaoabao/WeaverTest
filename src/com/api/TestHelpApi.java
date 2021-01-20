package com.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.api.doc.detail.util.ImageConvertUtil;
import com.api.email.util.LoggerUtils;
import com.engine.common.util.ParamUtil;
import com.engine.common.util.ServiceUtil;
import com.engine.email.service.EmailViewService;
import com.engine.email.service.impl.EmailViewServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.weaver.qfengx.*;
import com.weaver.qfengx.entity.Result;
import weaver.conn.RecordSet;
import weaver.general.ImageUtil;
import weaver.general.Util;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.rsa.security.RSA;

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
 * Created by YeShengtao on 2020/12/4 16:44
 */
@Path("seconddev/yst/test")
public class TestHelpApi {

    private static final LogUtils log = new LogUtils(TestHelpApi.class);

    @GET
    @Path("/getFilePreviewUrlForMobile")
    @Produces(MediaType.TEXT_PLAIN)
    public String getFilePreviewUrlForMobile(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        LoggerUtils.startTiming("emailView  getFilePreviewUrlForMobile");
        Map<String, Object> apidatas = new HashMap<String, Object>();
        try {
            String loginid = PropUtils.get("HxbankMailHelp", "loginid");
            User user = getUser(loginid);
            apidatas.putAll(getService(user).getFilePreviewUrlForMobile(user, ParamUtil.request2Map(request)));
            apidatas.put("status", "1");
        } catch (Exception e) {
            e.printStackTrace();
            apidatas.put("status", "0");
            apidatas.put("api_errormsg", "catch exception : " + e.getMessage());
        }
        LoggerUtils.endTiming("emailView  getFilePreviewUrlForMobile");
        return JSONObject.toJSONString(apidatas);
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String list(@Context HttpServletRequest request,
                       @Context HttpServletResponse response) {
        try {
            User user = HrmUserVarify.getUser(request, response);
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
                resMap.put("isCanPreviewForMobile", String.valueOf(isCanPreviewForMobile));
                dataList.add(resMap);
            });
            return JSON.toJSONString(Result.ok(dataList));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    private EmailViewService getService(User user) {
        return (EmailViewServiceImpl) ServiceUtil.getService(EmailViewServiceImpl.class, user);
    }

    private User getUser(String loginid) {
        Map<String, String> userMap = DaoUtils.executeQueryToMap("select id,password,lastname,sex,telephone,mobile,mobilecall,email,countryid,locationid,resourcetype,startdate,enddate,jobtitle,joblevel,seclevel,departmentid,subcompanyid1, managerid,assistantid  from hrmresource where loginid = ?", new Object[]{loginid});
        User user = new User();
        RSA rsa = new RSA();
        user.setUid(NumberUtils.parseInt(StringUtils.val(userMap.get("id"))));
        user.setLoginid(loginid);
        user.setPwd(rsa.encrypt((HttpServletRequest)null, StringUtils.val(userMap.get("password")), (String)null));
        user.setLastname(StringUtils.val(userMap.get("lastname")));
        user.setSex(StringUtils.val(userMap.get("sex")));
        user.setLanguage(7);
        user.setTelephone(StringUtils.val(userMap.get("telephone")));
        user.setMobile(StringUtils.val(userMap.get("mobile")));
        user.setMobilecall(StringUtils.val(userMap.get("mobilecall")));
        user.setEmail(StringUtils.val(userMap.get("email")));
        user.setCountryid(StringUtils.val(userMap.get("countryid")));
        user.setLocationid(StringUtils.val(userMap.get("locationid")));
        user.setResourcetype(StringUtils.val(userMap.get("resourcetype")));
        user.setStartdate(StringUtils.val(userMap.get("startdate")));
        user.setEnddate(StringUtils.val(userMap.get("enddate")));
        user.setJobtitle(StringUtils.val(userMap.get("jobtitle")));
        user.setJoblevel(StringUtils.val(userMap.get("joblevel")));
        user.setSeclevel(StringUtils.val(userMap.get("seclevel")));
        user.setUserDepartment(Util.getIntValue(StringUtils.val(userMap.get("departmentid")), 0));
        user.setUserSubCompany1(Util.getIntValue(StringUtils.val(userMap.get("subcompanyid1")), 0));
        user.setManagerid(StringUtils.val(userMap.get("managerid")));
        user.setAssistantid(StringUtils.val(userMap.get("assistantid")));
        user.setIsAdmin(false);
        return user;
    }

    private String getPreviewUrl(String mailid, String mailFileId) {
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
     * @param mailFileId 邮件附件id
     * @return
     */
    private static String getNewPreviewUrl(int mailFileId) {
        return "/docs/view/imageFileView.jsp?fileid=" + mailFileId;
    }

    /**
     * 判断扩展名是不是图片
     *
     * @param extName
     * @return true：是图片，false：不是
     */
    private static boolean isImageFile(String extName) {
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
    private static String getFileSize(String fileSize) {
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

    private String getDownloadUrl(String mailid, String mailFileId) {
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
    private Map<String, String> getfileTypeIcon(String fileType) {
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
}
