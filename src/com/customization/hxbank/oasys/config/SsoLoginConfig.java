package com.customization.hxbank.oasys.config;

import com.weaver.qfengx.PropUtils;
import weaver.general.BaseBean;
import weaver.general.Util;

/**
 * Created by YeShengtao on 2020/10/17 16:22
 */
public class SsoLoginConfig {
    private static BaseBean baseBean = new BaseBean();

    public static final String CONFIG_FILE_NAME = "HxbankSsoLogin";

    //    基本配置信息
    public static String emailUrl = "";
    public static String emailAppid = "";

    static {
        initProp();
    }

    public static void initProp() {
        loadProp();
        loadForm();
    }

    /**
     * 从prop配置文件加载
     */
    public static void loadProp() {
        baseBean.writeLog("加载配置文件 => " + CONFIG_FILE_NAME);
        emailUrl = Util.null2String(PropUtils.get(CONFIG_FILE_NAME, "email.url"));
        emailAppid = Util.null2String(PropUtils.get(CONFIG_FILE_NAME, "email.appid"));
    }

    /**
     * 从建模表单加载
     */
    public static void loadForm() {

    }
}
