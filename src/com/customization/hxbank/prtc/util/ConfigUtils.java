package com.customization.hxbank.prtc.util;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by YeShengtao on 2020/9/25 18:12
 */
public class ConfigUtils {

    // 配置文件名
    public static String propFilename = "prtc";

    public static Map<String, String> proxy = Maps.newHashMap();
    public static Map<String, String> http = Maps.newHashMap();

    public static String scannerdir = "";

    public static String trashdir = "";

    public static String cron = "";

    public static void propLoad() {
        http = PropUtils.regexMap(propFilename, "http\\..*$", x -> x.replace("http.", ""));
        proxy = PropUtils.regexMap(propFilename, "proxy\\..*$", x -> x.replace("proxy.", ""));
        scannerdir = PropUtils.get(propFilename, "scannerdir");
        cron = PropUtils.get(propFilename, "cron");
        trashdir = PropUtils.get(propFilename, "trashdir");
    }


    /**
     * 读取配置文件的配置参数
     */
    public static void initProp() {
        // 加载配置文件信息
        propLoad();
        // 加载表单信息
//        loadForm();
    }

}
