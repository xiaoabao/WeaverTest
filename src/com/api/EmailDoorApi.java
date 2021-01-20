package com.api;

import com.alibaba.fastjson.JSON;
import com.customization.hxbank.emailsys.service.OaDoorService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.weaver.qfengx.*;
import com.weaver.qfengx.entity.Result;
import weaver.general.BaseBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * OA门户相关API接口
 * Created by YeShengtao on 2020/9/24 16:58
 */
@Path("seconddev/hxbank/oa")
public class EmailDoorApi extends BaseBean {

    private OaDoorService oaDoorService = new OaDoorService();

    // 根据用户名生成用户头像
    @GET
    @Path("/image/header")
    @Produces(MediaType.APPLICATION_JSON)
    public String headerImage(@Context HttpServletRequest request,
                              @Context HttpServletResponse response) {
        try {
            String username = request.getParameter("username");
            String textRgb = request.getParameter("textRgb");
            String bgRgb = request.getParameter("bgRgb");
            int width = NumberUtils.parseInt(request.getParameter("w"), 120);
            int height = NumberUtils.parseInt(request.getParameter("h"), 120);
            if (StringUtils.isEmpty(username)) {
                return JSON.toJSONString(Result.paramErr());
            }
            if (StringUtils.isEmpty(textRgb)) {
                textRgb = "FFFFFF";
            }
            if (StringUtils.isEmpty(bgRgb)) {
                bgRgb = "0079FF";
            }
            if (!textRgb.matches("[0-9A-F]{6}")) {
                return JSON.toJSONString(Result.fail("textRgb is invaild"));
            }
            if (!bgRgb.matches("[0-9A-F]{6}")) {
                return JSON.toJSONString(Result.fail("bgRgb is invaild"));
            }
            ImageUtils.headImage(response.getOutputStream(), StringUtils.tailSubstring(username, 2), "#" + textRgb,
                    "#" + bgRgb, width, height);
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    // 邮件列表
    @GET
    @Path("/mail/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String mailList(@Context HttpServletRequest request,
                           @Context HttpServletResponse response) {
        try {
            String loginid = request.getParameter("loginid");
            String token = request.getParameter("token");
            int page = NumberUtils.parseInt(request.getParameter("page"), 1);
            int limit = NumberUtils.parseInt(request.getParameter("limit"), 10);
            int start = (page - 1) * limit;
            if (StringUtils.isEmpty(token) || StringUtils.isEmpty(loginid)) {
                return JSON.toJSONString(Result.paramErr());
            }
            // 校验token
            if (!oaDoorService.checkOaToken(token)) {
                return JSON.toJSONString(Result.tokenErr());
            }
            if (!oaDoorService.checkOaTokenIp(oaDoorService.queryToken(token), request)) {
                return JSON.toJSONString(Result.fail("IP：" + RequestUtils.ip(request) + " 不合法"));
            }
            List<String> fields = Lists.newArrayList(
                    "mailresource.waitdealnote", "mailresource.sendto", "mailresource.emlName", "mailresource.bccgroupids",
                    "mailresource.subject", "mailresource.wdremindtime", "mailresource.size_n", "mailresource.emlpath",
                    "mailresource.mr_uuid", "mailresource.readdate", "mailresource.ccsubcomids", "mailresource.id",
                    "mailresource.receivetime", "mailresource.priority", "mailresource.folderId", "mailresource.ccgroupids",
                    "mailresource.bccsubcomids", "mailresource.sendbcc", "mailresource.tosubcomids", "mailresource.status",
                    "mailresource.flag", "mailresource.attachmentNumber", "mailresource.waitdeal", "mailresource.bccdpids",
                    "mailresource.toids", "mailresource.timingdate", "mailresource.sendfrom", "mailresource.senddate",
                    "mailresource.mailAccountId", "mailresource.waitdealway", "mailresource.resourceid", "mailresource.ccdpids",
                    "mailresource.todpids", "mailresource.star", "mailresource.sendcc", "mailresource.bccids",
                    "mailresource.emltime", "mailresource.togroupids", "mailresource.waitdealtime, MAILCONTENT.MAILCONTENT as mailcontent"
            );
            String criteria = "left join HrmResource on HrmResource.id = mailresource.resourceid " +
                    "left join MAILCONTENT on MAILCONTENT.content_uuid = mailresource.content_uuid where isinternal = 1 and originalMailId is not null  and HrmResource.loginid = ?";
            List<Map<String, String>> dataMapList = DaoUtils.executeQueryToMapListPage("mailresource", criteria, fields, start, limit, loginid);
            int sum = NumberUtils.parseInt(DaoUtils.querySingleVal("select count(1) as sum from mailresource " +
                    "left join HrmResource on HrmResource.id = mailresource.resourceid " +
                    "where isinternal = 1 and originalMailId is not null  and HrmResource.loginid = ?", loginid));
            Map<String, Object> res = Maps.newHashMap();
            res.put("count", sum);
            for (Map<String, String> x : dataMapList) {
                x.put("showTime", DateUtils.relative(DateUtils.parse(StringUtils.val(x.get("senddate")),
                        "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd"));
                x.remove("rownumber");
                x.remove("mailtype");
                x.remove("isautoreceive");
                x.remove("CLASSIFICATION");
                x.remove("isSendApart");
                x.remove("isNewContent");
                x.remove("receiveNeedReceipt");
                x.remove("canview");
                x.remove("originalMailId");
                x.remove("needReceipt");
                x.remove("isInternal");
                x.remove("isTemp");
                x.remove("timingdatestate");
                x.remove("bccall");
                x.remove("messageid");
                x.remove("content");
                x.remove("recallState");
                x.remove("toall");
                x.remove("hasHtmlImage");
                x.remove("content_uuid");
                x.remove("ccall");
                x.remove("haseml");
                x.remove("mit_uuid");
                x.remove("op_hasRemind");
                x.remove("ccids");
            }
            res.put("array", dataMapList);
            return JSON.toJSONString(Result.ok(res));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

}
