package com.api;

import com.alibaba.fastjson.JSON;
import com.engine.common.util.ServiceUtil;
import com.engine.email.service.EmailViewService;
import com.engine.email.service.impl.EmailViewServiceImpl;
import com.weaver.qfengx.DaoUtils;
import com.weaver.qfengx.LogUtils;
import com.weaver.qfengx.NumberUtils;
import com.weaver.qfengx.StringUtils;
import com.weaver.qfengx.entity.Result;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
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
import java.util.Map;
import java.util.regex.Matcher;


/**
 * 邮件帮助中心列表获取
 * Created by YeShengtao on 2020/12/3 17:47
 */
@Path("/qfengx/seconddev/hxbank/email/help")
public class QfengxHelpApi extends BaseBean {

    private static final LogUtils log = new LogUtils(QfengxHelpApi.class);

    @GET
    @Path("/getFilePreviewUrlForMobile")
    @Produces(MediaType.TEXT_PLAIN)
    public String getFilePreviewUrlForMobile(@Context HttpServletRequest request, @Context HttpServletResponse response) {
       return JSON.toJSONString(Result.ok());
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String list(@Context HttpServletRequest request,
                       @Context HttpServletResponse response) {
        try {
            return JSON.toJSONString(Result.ok());
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }



    private EmailViewService getService(HttpServletRequest request, HttpServletResponse response) {
        User user = HrmUserVarify.getUser(request, response);// 需要增加的代码
        return getService(user);
    }

    private EmailViewService getService(User user) {
        return (EmailViewServiceImpl) ServiceUtil.getService(EmailViewServiceImpl.class, user);
    }

    private User getUser(String loginid) {
        Map<String, String> userMap = DaoUtils.executeQueryToMap("select " +
                "id,password,lastname,sex,telephone,mobile,mobilecall,email,countryid,locationid," +
                "resourcetype,startdate,enddate,jobtitle,joblevel,seclevel,departmentid,subcompanyid1, " +
                "managerid,assistantid  " +
                "from hrmresource where loginid = ?", loginid);
        UserManager userManager = new UserManager();
        return userManager.getUserByUserIdAndLoginType(NumberUtils.parseInt(StringUtils.val(userMap.get("id"))), "1");
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
