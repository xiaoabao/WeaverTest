package com.customization.hxbank.prtc.util;

import com.google.common.collect.Maps;
import okhttp3.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 请求工具类，底层依赖于OkHttp
 * Created by YeShengtao on 2020/9/25 17:41
 */
public class RequestUtils {

    /**
     * 上传文件以及参数，POST请求方式
     *
     * @param OkHttpClient 请求客户端
     * @param inputStream  上传文件输入流
     * @param params       上传文件其他相关参数信息
     */
    public static Response uploadFileAndParam(OkHttpClient client, String url, File file,
                                              Map<String, String> params) throws IOException {
        return uploadFileAndParam(client, url, file, Maps.newHashMap(), params);
    }

    /**
     * 上传文件以及参数，POST请求方式
     *
     * @param OkHttpClient 请求客户端
     * @param inputStream  上传文件输入流
     * @param headers      上传文件请求头相关参数信息
     * @param params       上传文件其他相关参数信息
     */
    public static Response uploadFileAndParam(OkHttpClient client, String url, File file,
                                              Map<String, String> headers, Map<String, String> params) throws IOException {
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        // 凭借请求头信息
        Headers.Builder hbuilder = new Headers.Builder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            hbuilder.add(entry.getKey(), entry.getValue());
        }
        // 拼接请求参数信息
        for (Map.Entry<String, String> entry : params.entrySet()) {
            multipartBodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody = null;
        if (file.exists()) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
            requestBody = multipartBodyBuilder
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), fileBody)
                    .build();


        } else {
            requestBody = multipartBodyBuilder
                    .setType(MultipartBody.FORM)
                    .build();
        }

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .headers(hbuilder.build())
                .build();
        return client.newCall(request).execute();
    }


    @Test
    public void test() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Map<String, String> params = Maps.newHashMap();
        params.put("requestid", "1");
        params.put("token", "1");
        params.put("fieldname", "1");
        params.put("user", "1");
        File file = new File("C:\\Users\\Qfeng\\Desktop\\down\\test.jpg");
        Response response = RequestUtils.uploadFileAndParam(client,
                "http://localhost:89/api/seconddev/hxbank/prtc/upload", file, params);
        System.out.println(response.body().string());
        System.out.println(response.isSuccessful());
    }


    @Test
    public void test01() {
//        String name = "user-requestid-fieldn--ame";
//        System.out.println(Splitter.on("-").trimResults().omitEmptyStrings().split(name).toString());
        File file = new File("C:\\Users\\Qfeng\\Desktop\\upload\\prtc\\user-requestid-fieldn--ame\\11.txt");
        file.delete();
    }
}
