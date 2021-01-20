package com.api;

import com.alibaba.fastjson.JSON;
import com.customization.hxbank.emailsys.config.SsoLoginConfig;
import com.google.common.collect.Maps;
import com.weaver.qfengx.RequestUtils;
import com.weaver.qfengx.StringUtils;
import com.weaver.qfengx.entity.Result;
import okhttp3.Response;
import weaver.general.BaseBean;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * 邮件系统单点登录API
 * Created by YeShengtao on 2020/10/14 16:02
 */
@Path("seconddev/hxbank/sso")
public class EmailToOaSsoApi extends BaseBean {

    @GET
    @Path("/oa/login")
    @Produces(MediaType.APPLICATION_JSON)
    public String oaLogin(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        try {
            SsoLoginConfig.initProp();
            // 当前登录用户
            User user = HrmUserVarify.getUser(request, response);
            if (user == null) {
                return JSON.toJSONString(Result.fail("用户未登录"));
            }
            writeLog("loginid => " + user.getLoginid());
            String url = SsoLoginConfig.oaUrl;
            String appid = SsoLoginConfig.oaAppid;
            if (StringUtils.isEmpty(url)) {
                writeLog("未配置URL");
                return JSON.toJSONString(Result.fail("未配置URL"));
            }
            if (StringUtils.isEmpty(appid)) {
                writeLog("未配置APPID");
                return JSON.toJSONString(Result.fail("未配置APPID"));
            }
            writeLog("url => " + url);
            writeLog("appid => " + appid);
            // 模拟登录，获取token
            Map<String, String> params = Maps.newHashMap();
            params.put("appid", appid); // 统一认证配置的appid
            params.put("loginid", user.getLoginid());
            Response res = RequestUtils.doGet(url, params);
            String respStr = res.body().string();
            if (res.isSuccessful() && res.code() == 200) { // 获取到token
                return JSON.toJSONString(Result.ok(respStr));
            } else {
                writeLog("单点登录失败: " + respStr);
                return JSON.toJSONString(Result.fail("单点登录失败: " + respStr));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

}
