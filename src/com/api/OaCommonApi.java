package com.api;

import com.alibaba.fastjson.JSON;
import com.google.common.io.ByteStreams;
import com.weaver.general.Util;
import com.weaver.qfengx.CryptUtils;
import com.weaver.qfengx.DaoUtils;
import com.weaver.qfengx.IDUtils;
import com.weaver.qfengx.StringUtils;
import com.weaver.qfengx.entity.Result;
import weaver.file.ImageFileManager;
import weaver.rsa.security.RSA;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * 公共参数相关API
 * Created by YeShengtao on 2020/9/27 10:09
 */
@Path("seconddev/hxbank/common")
public class OaCommonApi {

    @GET
    @Path("/cpk")
    @Produces(MediaType.TEXT_PLAIN)
    public String cpk(@Context HttpServletRequest request,
                          @Context HttpServletResponse response) {
        return JSON.toJSONString(Result.ok(new RSA().getRSA_PUB()));
    }

    @POST
    @Path("/secret")
    @Produces(MediaType.TEXT_PLAIN)
    public String secret(@Context HttpServletRequest request,
                      @Context HttpServletResponse response) {
        String secrit = request.getParameter("secrit");
        String spk = request.getParameter("spk");
        if (StringUtils.isEmpty(secrit) || StringUtils.isEmpty(spk)) {
            return JSON.toJSONString(Result.paramErr());
        }
        String secret = new RSA().encrypt(null, secrit, null, "utf-8", spk, false);
        return JSON.toJSONString(Result.ok(secret));
    }

    @GET
    @Path("/xml/data")
    @Produces(MediaType.APPLICATION_JSON)
    public String xmlData(@Context HttpServletRequest request,
                         @Context HttpServletResponse response) {

        try {
            String rid = request.getParameter("rid");
            List<Map<String, String>> dataMapList = DaoUtils.executeQueryToMapList("select " +
                    "workflow_requestlog.nodeid,workflow_nodebase.nodename,workflow_requestlog.logtype, " +
                    "workflow_requestlog.annexdocids, hrmdepartment.departmentname, hrmdepartment.id as departmentid, " +
                    "hrmsubcompany.subcompanyname, hrmsubcompany.id as subcompanyid, workflow_requestlog.remark, " +
                    "workflow_requestlog.handwrittensign, workflow_requestlog.operator, hrmresource.lastname as operatorname, " +
                    "workflow_requestlog.receivedpersonids,workflow_requestlog.receivedpersons, " +
                    "workflow_requestlog.operatedate,workflow_requestlog.operatetime " +
                    "from workflow_requestlog " +
                    "left join hrmresource on hrmresource.id = workflow_requestlog.operator " +
                    "left join hrmdepartment on hrmdepartment.id = hrmresource.departmentid " +
                    "left join hrmsubcompany on hrmsubcompany.id = hrmresource.subcompanyid1 " +
                    "left join workflow_nodebase on workflow_nodebase.id = workflow_requestlog.nodeid " +
                    "where workflow_requestlog.requestid = ? " +
                    "order by workflow_requestlog.operatedate desc,workflow_requestlog.operatetime desc", rid);
            for (Map<String, String> map : dataMapList) {
                if (StringUtils.isNotEmpty(StringUtils.val(map.get("handwrittensign")))) {
                    ImageFileManager imageFileManager = new ImageFileManager();
                    imageFileManager.getImageFileInfoById(Util.getIntValue(StringUtils.val(map.get("handwrittensign"))));
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ByteStreams.copy(imageFileManager.getInputStream(), bos);
                    map.put("handwrittensign", CryptUtils.toBase64(bos.toByteArray()));
                }
                switch (StringUtils.val(map.get("logtype"))) {
                    case "0": { map.put("logtype", "批准");break; }
                    case "1": { map.put("logtype", "保存");break; }
                    case "2": { map.put("logtype", "提交");break; }
                    case "3": { map.put("logtype", "退回");break; }
                    case "4": { map.put("logtype", "重新打开");break; }
                    case "5": { map.put("logtype", "删除");break; }
                    case "6": { map.put("logtype", "激活");break; }
                    case "7": { map.put("logtype", "转发");break; }
                    case "9": { map.put("logtype", "批注");break; }
                    case "a": { map.put("logtype", "意见征询");break; }
                    case "b": { map.put("logtype", "意见征询回复");break; }
                    case "e": { map.put("logtype", "强制归档");break; }
                    case "h": { map.put("logtype", "转办");break; }
                    case "i": { map.put("logtype", "干预");break; }
                    case "j": { map.put("logtype", "转办反馈");break; }
                    case "s": { map.put("logtype", "督办");break; }
                    case "t": { map.put("logtype", "抄送");break; }
                }
            }
            return JSON.toJSONString(Result.ok(dataMapList));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }

//        try {
//            response.sendRedirect("/baidu");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
//                "<datas>\n" +
//                " <data>\n" +
//                "  <regulation_id>1125908247656480</regulation_id>\n" +
//                "  <regulation_name>其他公司-差旅</regulation_name>\n" +
//                "  <regulation_status>1</regulation_status>\n" +
//                "  <scene_type>2</scene_type>\n" +
//                "  <is_use_quota>0</is_use_quota>\n" +
//                "  <is_approve>1</is_approve>\n" +
//                " </data>\n" +
//                " <data>\n" +
//                "  <regulation_id>1125907546637874</regulation_id>\n" +
//                "  <regulation_name>HCH测试用车-差旅</regulation_name>\n" +
//                "  <regulation_status>1</regulation_status>\n" +
//                "  <scene_type>2</scene_type>\n" +
//                "  <is_use_quota>0</is_use_quota>\n" +
//                "  <is_approve>1</is_approve>\n" +
//                " </data>\n" +
//                " <data>\n" +
//                "  <regulation_id>1125908220304449</regulation_id>\n" +
//                "  <regulation_name>HCH测试用车-市内交通</regulation_name>\n" +
//                "  <regulation_status>1</regulation_status>\n" +
//                "  <scene_type>1</scene_type>\n" +
//                "  <is_use_quota>0</is_use_quota>\n" +
//                "  <is_approve>0</is_approve>\n" +
//                " </data>\n" +
//                " <data>\n" +
//                "  <regulation_id>1125908219924151</regulation_id>\n" +
//                "  <regulation_name>HCH测试用车-加班</regulation_name>\n" +
//                "  <regulation_status>1</regulation_status>\n" +
//                "  <scene_type>3</scene_type>\n" +
//                "  <is_use_quota>0</is_use_quota>\n" +
//                "  <is_approve>0</is_approve>\n" +
//                " </data>\n" +
//                " <data>\n" +
//                "  <regulation_id>1125907493048864</regulation_id>\n" +
//                "  <regulation_name>测试用车</regulation_name>\n" +
//                "  <regulation_status>1</regulation_status>\n" +
//                "  <scene_type>2</scene_type>\n" +
//                "  <is_use_quota>0</is_use_quota>\n" +
//                "  <is_approve>1</is_approve>\n" +
//                " </data>\n" +
//                "</datas>";
    }

    /**
     * 雪花算法生成ID，新建类型配置时调用使用
     */
    @GET
    @Path("/snowFlake/id")
    @Produces(MediaType.APPLICATION_JSON)
    public String typeId(@Context HttpServletRequest request,
                         @Context HttpServletResponse response) {
        try {
            // 根据雪花算法 生成ID
            return JSON.toJSONString(Result.ok(IDUtils.snowFlakeId()));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

}
