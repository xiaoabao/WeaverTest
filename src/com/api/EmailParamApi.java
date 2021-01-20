package com.api;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.weaver.qfengx.DaoUtils;
import com.weaver.qfengx.entity.Result;
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
 * 参数查询API
 * Created by YeShengtao on 2020/10/20 17:10
 */
@Path("seconddev/hxbank/param")
public class EmailParamApi {

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
}
