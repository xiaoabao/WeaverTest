package com.customization.hxbank.prtc.client;

import com.customization.hxbank.prtc.entity.UploadFileEntity;
import com.customization.hxbank.prtc.util.ConfigUtils;
import com.customization.hxbank.prtc.util.FileUtils;
import com.customization.hxbank.prtc.util.StringUtils;
import com.customization.hxbank.prtc.util.ThreadUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 印控机文件定时上传任务
 * Created by YeShengtao on 2020/9/27 10:24
 */
@Slf4j
@DisallowConcurrentExecution
public class PrtcUploadJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            log.info("开始执行定时任务 {}", jobExecutionContext.getJobDetail().getKey().getName());
            // 扫描文件目录
            File dir = new File(ConfigUtils.scannerdir);
            log.info("扫描目录 => {}", ConfigUtils.scannerdir);
            if (!dir.isDirectory()) {
                log.info("===> 配置的扫描目录不存在或者为非目录");
                return;
            }
            // 扫描获取所有的文件信息
            List<File> allFile = FileUtils.allFile(dir);
            log.info("本次共扫描到 {} 个文件", allFile.size());
            // 创建请求客户端
            OkHttpClient client = getClient();

            List<UploadFileEntity> fileEntities = allFile.stream().map((x) -> {
                if (Objects.isNull(x)) {
                    log.info("{} ===> file is null", Thread.currentThread().getName());
                    return null;
                }
                if (!x.exists()) {
                    log.info("{} ===> 文件不存在", Thread.currentThread().getName());
                    return null;
                }
                // 分析出参数信息
                String parentName = x.getParentFile().getName();
                log.info("{} ===> parentName => {}, file => {}", Thread.currentThread().getName(),
                        parentName, x.getName());
                // 对文件所在目录进行目录名拆分
                List<String> paramList = Lists.newArrayList(Splitter.on("-")
                        .trimResults()
                        .omitEmptyStrings()
                        .split(parentName));
                if (paramList.size() != 3) {
                    log.info("{} ===> 文件名规则不匹配", Thread.currentThread().getName());
                    return null;
                }
                return new UploadFileEntity()
                        .setFile(x)
                        .setFieldname(paramList.get(2))
                        .setRequestid(paramList.get(1))
                        .setUser(paramList.get(0));
            }).filter(Objects::nonNull).collect(Collectors.toList());

            log.info("实际有效文件数 => {}", fileEntities.size());
            CountDownLatch endGate = new CountDownLatch(fileEntities.size()); // 保证所有请求线程执行完成
            for (UploadFileEntity entity : fileEntities) {
                ThreadUtils.getPoolExecutor()
                        .execute(new UploadHandler(entity, client, endGate));
            }
            endGate.await();
            log.info("定时任务 {} 执行结束", jobExecutionContext.getJobDetail().getKey().getName());
        } catch (Exception e) {
            e.printStackTrace();
            log.info("{} 任务执行异常 => {}", jobExecutionContext.getJobDetail().getKey().getName(), e.getMessage());
        }
    }


    private OkHttpClient getClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        // 判断是否需要开启代理
        log.info(String.format("代理状态 => %s", ConfigUtils.proxy.get("enable")));
        if (StringUtils.equals(ConfigUtils.proxy.get("enable"), "true")) { // 需要开启代理
            log.info("开启代理设置 => ip: {}, port: {}", ConfigUtils.proxy.get("ip"), ConfigUtils.proxy.get("port"));
            clientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ConfigUtils.proxy.get("ip"),
                    Integer.parseInt(ConfigUtils.proxy.get("port")))));
            // 判断是否有账号
            String username = ConfigUtils.proxy.get("username");
            if (StringUtils.isNotEmpty(username)) {
                log.info("代理服务器用户信息 => username: {}, password: {}", ConfigUtils.proxy.get("username"),
                        ConfigUtils.proxy.get("password"));
                clientBuilder.authenticator((route, response) -> {
                    String credential = Credentials.basic(username,
                            ConfigUtils.proxy.get("password"));
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                });
            }
        }
        // 判断是否有超时
        if (StringUtils.isNotEmpty(ConfigUtils.http.get("connectTimeout")))
            clientBuilder.connectTimeout(Integer.parseInt(ConfigUtils.http.get("connectTimeout")), TimeUnit.SECONDS);
        if (StringUtils.isNotEmpty(ConfigUtils.http.get("readTimeout")))
            clientBuilder.connectTimeout(Integer.parseInt(ConfigUtils.http.get("readTimeout")), TimeUnit.SECONDS);
        return clientBuilder.build();
    }

}
