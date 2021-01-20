package com.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.api.doc.detail.util.DocDownloadCheckUtil;
import com.customization.hxbank.oasys.entity.PortalBannerInfo;
import com.customization.hxbank.oasys.service.QuickLookService;
import com.engine.common.util.ServiceUtil;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.weaver.qfengx.DaoUtils;
import com.weaver.qfengx.ImageFileUtils;
import com.weaver.qfengx.NumberUtils;
import com.weaver.qfengx.StringUtils;
import com.weaver.qfengx.entity.Result;
import weaver.file.ImageFileManager;
import weaver.general.BaseBean;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.general.Util;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by YeShengtao on 2020/10/29 10:31
 */
@Path("customization/portal")
public class OaPortalApi extends BaseBean {

    private static final String IAMAGE_URL_PRIFIX = "/api/customization/portal/image";
    private static final String IAMAGE_URL_PRIFIX_TEMP = "/api/customization/portal/file";


    @GET
    @Path("getbannerinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public String getbannerinfo(@Context HttpServletRequest request,
                                @Context HttpServletResponse response) {
        try {
            List<PortalBannerInfo> portalBannerInfoList = DaoUtils.queryTableToBean("uf_tpurl", PortalBannerInfo.class);
            return JSON.toJSONString(Result.ok(
                    // 遍历图片将docId转换
                    portalBannerInfoList.stream().map(x -> {
                        Map<String, String> newMap = Maps.newHashMap();
                        String docid = x.getBannerImage();
                        newMap.put("bannerimageurl", IAMAGE_URL_PRIFIX + "?id=" + docid);
                        String imagefileid = DaoUtils.querySingleVal("select imagefileid from DocImageFile where docid = ? order by versionid desc", docid);
                        newMap.put("bannerimageurltemp", IAMAGE_URL_PRIFIX_TEMP + "?fileid=" + imagefileid);
                        newMap.put("targeturl", x.getTargetUrl());
                        return newMap;
                    }).collect(Collectors.toList())
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }


    @GET
    @Path("notification")
    @Produces(MediaType.APPLICATION_JSON)
    public String notification(@Context HttpServletRequest request,
                                @Context HttpServletResponse response) {
        try {
            User user = HrmUserVarify.getUser(request, response);
            QuickLookService quickLookService = ServiceUtil.getService(QuickLookService.class, user);
            Map<String, Object> apidatas = Maps.newHashMap();
            Map<String, Object> params = Maps.newHashMap();
            params.put("seccategorytype", "1");
            params.put("pageIndex", "1");
            params.put("pageSize", "1");
            apidatas.putAll(quickLookService.getDocInfoList(params));
            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(apidatas.get("list")));
            return JSON.toJSONString(Result.ok(jsonArray.get(0)));
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    @GET
    @Path("image")
    public String image(@Context HttpServletRequest request,
                                @Context HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            if (StringUtils.isEmpty(id)) {
                return JSON.toJSONString(Result.paramErr());
            }
            int val = NumberUtils.parseInt(DaoUtils.querySingleVal("select count(1) as sum from uf_tpurl where bannerImage like ?", id));
            if ( val <= 0) {
                return JSON.toJSONString(Result.fail());
            }
            ImageFileManager imageFileManager = ImageFileUtils.imageFileManager(id);
            if (imageFileManager == null) {
                return JSON.toJSONString(Result.fail("文件不存在"));
            }
            // 设置文件名
            String userAgent = request.getHeader("User-Agent");
            String filename = imageFileManager.getImageFileName();
            writeLog("获取文件名 => " + filename);
            if (userAgent.toUpperCase().contains("MSIE") || userAgent.toUpperCase().contains("TRIDENT")) //针对IE或IE为内核的浏览器
                filename = java.net.URLEncoder.encode(filename, "UTF-8");
            else
                filename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);//谷歌控制版本
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Disposition", "attachment;filename=" + filename);

            // 获取文件流
            ByteStreams.copy(imageFileManager.getInputStream(), response.getOutputStream());
            return JSON.toJSONString(Result.ok());
        } catch (Exception e) {
            e.printStackTrace();
            return JSON.toJSONString(Result.exception(e.getMessage()));
        }
    }

    @GET
    @Path("file")
    public void file(@Context HttpServletRequest req,
                       @Context HttpServletResponse res) {
        //int fileid = Util.getIntValue(req.getParameter("fileid"));
        int fileid = Util.getIntValue(DocDownloadCheckUtil.getDownloadfileid(req), -1);
        new BaseBean().writeLog("---------- FileDownloadForNews ---------fileid=" + fileid);
        if(fileid <= 0) return;
        InputStream imagefile = null;
        try {
            ImageFileManager ifm = new ImageFileManager();
            ifm.getImageFileInfoById(fileid);
            imagefile = ifm.getInputStream();
            String filename = ifm.getImageFileName();;
            String contenttype = "";
            if(filename.toLowerCase().endsWith(".gif")) {
                contenttype = "image/gif";
                res.addHeader("Cache-Control", "private, max-age=8640000");
            }else if(filename.toLowerCase().endsWith(".png")) {
                contenttype = "image/png";
                res.addHeader("Cache-Control", "private, max-age=8640000");
            }else if(filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                contenttype = "image/jpg";
                res.addHeader("Cache-Control", "private, max-age=8640000");
            }else if(filename.toLowerCase().endsWith(".bmp")) {
                contenttype = "image/bmp";
                res.addHeader("Cache-Control", "private, max-age=8640000");
            }
            ServletOutputStream out = res.getOutputStream();
            res.setContentType(contenttype);
            res.setHeader("content-disposition", "attachment; filename=\"" +  new String(filename.replaceAll("<", "").replaceAll(">", "").replaceAll("&lt;", "").replaceAll("&gt;", "").getBytes("UTF-8"),"ISO-8859-1")+"\"");
            int byteread;
            byte data[] = new byte[1024];
            while ((byteread = imagefile.read(data)) != -1) {
                out.write(data, 0, byteread);
                out.flush();
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            new BaseBean().writeLog(e);
        } finally {
            if(imagefile != null){
                try{
                    imagefile.close();
                }catch(Exception e){
                    new BaseBean().writeLog(e);
                }
            }
        }
    }
}
