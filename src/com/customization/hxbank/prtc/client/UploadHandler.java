package com.customization.hxbank.prtc.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.customization.hxbank.prtc.entity.UploadFileEntity;
import com.customization.hxbank.prtc.util.ConfigUtils;
import com.customization.hxbank.prtc.util.RequestUtils;
import com.customization.hxbank.prtc.util.StringUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * 执行文件上传任务Runnable
 * Created by YeShengtao on 2020/9/27 12:07
 */
@Slf4j
public class UploadHandler implements Runnable {

    private UploadFileEntity entity = null;
    private OkHttpClient client = null;
    private CountDownLatch endGate = null;

    public UploadHandler(UploadFileEntity entity, OkHttpClient client, CountDownLatch endGate) {
        this.entity = entity;
        this.client = client;
        this.endGate = endGate;
    }

    @Override
    public void run() {
        try {
            // 拼接参数Map
            Map<String, String> params = Maps.newHashMap();
            params.put("requestid", entity.getRequestid());
            params.put("token", ConfigUtils.http.get("token"));
            params.put("fieldname", entity.getFieldname());
            params.put("user", entity.getUser());
            // 上传文件
            Response response = RequestUtils.uploadFileAndParam(client, ConfigUtils.http.get("url"), entity.getFile(), params);
            String body = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                JSONObject data = JSON.parseObject(body);
                if (StringUtils.equals(data.getString("result"), "0")) { // 操作成功
                    log.info("{} ===> 操作成功删除文件 => {}", Thread.currentThread().getName(), entity.getFile().getAbsolutePath());
                    // TODO 先不删除文件，移动文件到回收目录中，定期删除回收目录
                    entity.getFile().delete(); //移动文件到回收目录中
                } else { // 操作失败
                    log.info("{} ===> 操作失败 => {}", Thread.currentThread().getName(), body);
                }
            } else {
                log.info("{} ===> 文件上传失败 => code: {}, {}", Thread.currentThread().getName(), response.code(), body);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("{} ===> 文件上传异常 => {}", Thread.currentThread().getName(), e.getMessage());
        } finally {
            endGate.countDown();
        }
    }


}
