package com.customization.hxbank.prtc.client;

import com.customization.hxbank.prtc.util.ConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * 印控机客户端定时扫描目录上传
 * Created by YeShengtao on 2020/9/25 16:29
 */
@Slf4j
public class PrtcHttpClient {

    private static final String CONFIGFILE = "frtc";

    public static void main(String[] args) throws SchedulerException {
        // 加载配置文件
        ConfigUtils.initProp();
        // 创建任务调度器
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        // 创建任务
        JobDetail job = JobBuilder.newJob(PrtcUploadJob.class)
                .withIdentity("印控机文件上传任务")
                .build();
        log.info("cron表达式 => {}", ConfigUtils.cron);
        // 创建定时器
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("印控机文件上传定时器")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(ConfigUtils.cron))
                .build();
        scheduler.scheduleJob(job, trigger);
        scheduler.start();
    }

}
