package com.api;

import com.alibaba.fastjson.JSON;
import com.weaver.qfengx.entity.Result;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.customization.hxbank.oasys.service.MyWorkPlanCalendarService;
import com.customization.hxbank.oasys.service.OaDoorService;
import com.customization.hxbank.oasys.service.QuickLookService;
import com.customization.hxbank.oasys.workflow.entity.ApiWorkflowRequestInfo;
import com.customization.hxbank.oasys.workflow.publicApi.WorkflowRequestTodoPA;
import com.customization.hxbank.oasys.workflow.publicApi.impl.WorkflowRequestListPAImpl;
import com.engine.common.util.ServiceUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.weaver.qfengx.*;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;

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
public class OaDoorApi extends BaseBean {

    private OaDoorService oaDoorService = new OaDoorService();
    private WorkflowRequestTodoPA requestTodoPA = new WorkflowRequestListPAImpl();

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

    // 工作微博列表
    @GET
    @Path("/blog/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String blogList(@Context HttpServletRequest request,
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
                    "blog_discuss.createtime", "blog_discuss.content", "blog_discuss.workdate", "blog_discuss.createdate", "blog_discuss.isReplenish",
                    "blog_discuss.id", "blog_discuss.userid", "blog_discuss.lastUpdatetime", "HrmResource.lastname", "HrmDepartment.departmentname", "HrmSubCompany.subcompanyname"
            );
            String criteria = "left join HrmResource on blog_discuss.userid = HrmResource.id left join HrmDepartment on HrmResource.departmentid = HrmDepartment.id " +
                    "left join HrmSubCompany on HrmResource.subcompanyid1 = HrmSubCompany.id where HrmResource.loginid = ?";
            List<Map<String, String>> dataMapList = DaoUtils.executeQueryToMapListPage("blog_discuss", criteria, fields, start, limit, loginid);
            int sum = NumberUtils.parseInt(DaoUtils.querySingleVal("select count(1) as sum from blog_discuss " +
                    "left join HrmResource on blog_discuss.userid = HrmResource.id " +
                    "left join HrmDepartment on HrmResource.departmentid = HrmDepartment.id " +
                    "left join HrmSubCompany on HrmResource.subcompanyid1 = HrmSubCompany.id " +
                    "where HrmResource.loginid = ?", loginid));
            Map<String, Object> res = Maps.newHashMap();
            res.put("count", sum);
            for (Map<String, String> x : dataMapList) {
                String datetime = StringUtils.val(x.get("createdate")) + " " + StringUtils.val(x.get("createtime"));
                x.put("showTime", DateUtils.relative(DateUtils.parse(datetime, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd"));
                // 去除rownumber、comefrom、score
                x.remove("rownumber");
                x.remove("comefrom");
                x.remove("score");
            }
            res.put("array", dataMapList);
            return JSON.toJSONString(Result.ok(res));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    // 事务导览建模表
    @GET
    @Path("/swkg/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String swkgList(@Context HttpServletRequest request,
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
                    "uf_swgk.modedatamodifier", "uf_swgk.xtjdclqd", "uf_swgk.sxlxrjdh", "uf_swgk.sqdw",
                    "uf_swgk.formmodeid", "uf_swgk.gkfw, uf_swgk.xtzhsydwjmb, uf_swgk.cjsj", "uf_swgk.zcblsxsj",
                    "uf_swgk.modedatacreatedate", "uf_swgk.swx", "uf_swgk.tbryjdh", "uf_swgk.jtgzsx", "uf_swgk.zdyjjwh",
                    "uf_swgk.bz", "uf_swgk.id", "uf_swgk.sqbm", "uf_swgk.qxysmqk", "uf_swgk.sfxycsfzrsp", "uf_swgk.cjr",
                    "uf_swgk.modedatacreater", "uf_swgk.blqd", "uf_swgk.gzsxgs", "uf_swgk.modedatacreatetime", "uf_swgk.modedatamodifydatetime"
            );
            String criteria = "left join HrmResource on HrmResource.loginid = ? where uf_swgk.gkfw = 0 or " +
                    "(uf_swgk.sqdw = HrmResource.subcompanyid1 and uf_swgk.gkfw = 1) or (uf_swgk.sqbm = HrmResource.departmentid and uf_swgk.gkfw = 2)";
            List<Map<String, String>> dataMapList = DaoUtils.executeQueryToMapListPage("uf_swgk", criteria, fields, start, limit, loginid);
            int sum = NumberUtils.parseInt(DaoUtils.querySingleVal("select count(1) as sum from uf_swgk " +
                    "left join HrmResource on HrmResource.loginid = ? " +
                    "where uf_swgk.gkfw = 0 or (uf_swgk.sqdw = HrmResource.subcompanyid1 and uf_swgk.gkfw = 1) " +
                    "or (uf_swgk.sqbm = HrmResource.departmentid and uf_swgk.gkfw = 2)", loginid));
            Map<String, Object> res = Maps.newHashMap();
            res.put("count", sum);
            for (Map<String, String> x : dataMapList) {
                String datetime = StringUtils.val(x.get("modedatacreatedate")) + " " + StringUtils.val(x.get("modedatacreatetime"));
                x.put("showTime", DateUtils.relative(DateUtils.parse(datetime, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd"));

                // 去除rownumber、comefrom、score
                x.remove("rownumber");
                x.remove("MODEUUID");
                x.remove("modedatacreatertype");
                x.remove("requestId");
            }
            res.put("array", dataMapList);
            return JSON.toJSONString(Result.ok(res));
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
            String MAILDETAILSSOURL = PropUtils.get("EcologyIpAddressConfig", "MAILDETAILSSOURL");
            String IPADDRESS = PropUtils.get("EcologyIpAddressConfig", "IPADDRESS");

            for (Map<String, String> x : dataMapList) {
                // 增加详情请求地址
                x.put("pcurlsrc", IPADDRESS + StringUtils.replace(MAILDETAILSSOURL, "${id}", x.get("id")));
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


    // 待办列表
    @GET
    @Path("/workflow/todo/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String workflowTodoList(@Context HttpServletRequest request,
                                   @Context HttpServletResponse response) {
        try {
            String loginid = request.getParameter("loginid");
            String token = request.getParameter("token");
            int page = NumberUtils.parseInt(request.getParameter("page"), 1);
            int limit = NumberUtils.parseInt(request.getParameter("limit"), 10);
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
            // 根据loginid查询userid
            int userid = NumberUtils.parseInt(DaoUtils.querySingleVal("select id from HrmResource where loginid = ?", loginid));
            boolean isMergeShow = "true".equals(Util.null2String(request.getParameter("isMergeShow")));
            boolean isNeedOs = true;
            Map<String, String> conditions = Maps.newHashMap();
            if (request.getParameter("conditions") != null && !"".equals(request.getParameter("conditions").trim())) {
                conditions = JSONObject.parseObject(request.getParameter("conditions"), new TypeReference<Map<String, String>>() {
                });
            }
            User user = User.getUser(userid, 0);
            List<ApiWorkflowRequestInfo> toDoWorkflowRequestList = requestTodoPA.getToDoWorkflowRequestList(page, limit, user,
                    conditions, isMergeShow, isNeedOs);
            for (ApiWorkflowRequestInfo x : toDoWorkflowRequestList) {
                x.setShowTime(DateUtils.relative(DateUtils.parse(x.getReceiveTime(), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd"));
            }
            long dataSize = requestTodoPA.getToDoWorkflowRequestCount(user, conditions, isMergeShow, isNeedOs);
            Map<String, Object> resMap = Maps.newHashMap();
            resMap.put("count", dataSize);
            resMap.put("array", toDoWorkflowRequestList);
            return JSONObject.toJSONString(Result.ok(resMap));

        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    // 通知公告 文件预览
    @GET
    @Path("/doc/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String docBrowser(@Context HttpServletRequest request,
                             @Context HttpServletResponse response) {
        try {
            String loginid = request.getParameter("loginid");
            String token = request.getParameter("token");
            // 'quicklook'(文件预览) (0) or 'notification' (通知公告) (1)
            String type = request.getParameter("type");
            int page = NumberUtils.parseInt(request.getParameter("page"), 1);
            int limit = NumberUtils.parseInt(request.getParameter("limit"), 10);
            if (StringUtils.isEmpty(loginid) || StringUtils.isEmpty(type) || StringUtils.isEmpty(token)) {
                return JSON.toJSONString(Result.paramErr());
            }
            // 校验token
            if (!oaDoorService.checkOaToken(token)) {
                return JSON.toJSONString(Result.tokenErr());
            }
            if (!oaDoorService.checkOaTokenIp(oaDoorService.queryToken(token), request)) {
                return JSON.toJSONString(Result.fail("IP：" + RequestUtils.ip(request) + " 不合法"));
            }
            // 根据loginid查询userid
            int userid = NumberUtils.parseInt(DaoUtils.querySingleVal("select id from HrmResource where loginid = ?", loginid));
            Map<String, Object> apidatas = Maps.newHashMap();
            //获取当前用户
            User user = User.getUser(userid, 0);
            Map<String, Object> params = Maps.newHashMap();
            params.put("seccategorytype", type);
            params.put("pageIndex", Integer.toString(page));
            params.put("pageSize", Integer.toString(limit));
            QuickLookService quickLookService = ServiceUtil.getService(QuickLookService.class, user);
            apidatas.putAll(quickLookService.getDocInfoList(params));
            Map<String, Object> resMap = Maps.newHashMap();
            resMap.put("count", NumberUtils.parseInt(StringUtils.val(apidatas.get("count"))));
            resMap.put("array", apidatas.get("list"));
            return JSON.toJSONString(Result.ok(resMap));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    // 日程列表
    @GET
    @Path("/calendar/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String calendarList(@Context HttpServletRequest request,
                               @Context HttpServletResponse response) {
        try {
            String loginid = request.getParameter("loginid");
            String token = request.getParameter("token");
            String selectDate = request.getParameter("selectDate");
            String workPlanType = request.getParameter("workPlanType");
            String viewType = request.getParameter("viewType");
            if (StringUtils.isEmpty(token) || StringUtils.isEmpty(loginid) || StringUtils.isEmpty(selectDate)) {
                return JSON.toJSONString(Result.paramErr());
            }
            // 校验token
            if (!oaDoorService.checkOaToken(token)) {
                return JSON.toJSONString(Result.tokenErr());
            }
            if (!oaDoorService.checkOaTokenIp(oaDoorService.queryToken(token), request)) {
                return JSON.toJSONString(Result.fail("IP：" + RequestUtils.ip(request) + " 不合法"));
            }
            // 根据loginid查询userid
            int userid = NumberUtils.parseInt(DaoUtils.querySingleVal("select id from HrmResource where loginid = ?", loginid));
            Map<String, Object> apidatas = Maps.newHashMap();
            User user = User.getUser(userid, 0);
            Map<String, Object> params = Maps.newHashMap();
            params.put("selectUser", userid);
            params.put("selectDate", selectDate);
            params.put("workPlanType", workPlanType);
            params.put("viewType", viewType);
//            params.put("beginDate", beginDate);
//            params.put("endDate", endDate);
            MyWorkPlanCalendarService myWorkPlanCalendarService = ServiceUtil.getService(MyWorkPlanCalendarService.class, user);
            apidatas.putAll(myWorkPlanCalendarService.getMyCalendar(params));
            return JSON.toJSONString(Result.ok(apidatas));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

}
