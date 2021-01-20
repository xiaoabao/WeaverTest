package com.api.doc.web;

import com.alibaba.fastjson.JSONObject;
import com.api.doc.service.impl.NoticeServiceImpl;
import com.engine.common.util.ParamUtil;
import com.engine.common.util.ServiceUtil;
import com.engine.workflow.util.CommonUtil;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.HashMap;
import java.util.Map;

@Path("/docs")
public class DocList {
    private NoticeServiceImpl getDocListService (HttpServletRequest request,HttpServletResponse response){
        return (NoticeServiceImpl) ServiceUtil.getService(NoticeServiceImpl.class, CommonUtil.getUserByRequest(request,response));
    }

    @Path("/MyDocList")
    @GET
    public String getDocList(@Context HttpServletRequest request, @Context HttpServletResponse response){
        String result=null;
        User user= HrmUserVarify.getUser(request,response);
        Map<String,Object> map = new HashMap<String,Object>();
        Map map1= ParamUtil.request2Map(request);
        map = getDocListService(request,response).getDocList(map1,user);
        return JSONObject.toJSONString(map);
    }
}
