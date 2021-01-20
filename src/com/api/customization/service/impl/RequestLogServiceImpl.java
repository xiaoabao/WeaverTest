package com.api.customization.service.impl;

import com.weaver.qfengx.LogUtils;
import com.weaverboot.frame.ioc.anno.classAnno.WeaIocReplaceComponent;
import com.weaverboot.frame.ioc.anno.methodAnno.WeaReplaceAfter;
import com.weaverboot.frame.ioc.handler.replace.weaReplaceParam.impl.WeaAfterReplaceParam;

@WeaIocReplaceComponent("requestLogService")
public class RequestLogServiceImpl {

    private static LogUtils log = new LogUtils(RequestLogServiceImpl.class);

    @WeaReplaceAfter(value = "/api/workflow/reqform/getRequestLogList", order = 1)
    public String after(WeaAfterReplaceParam weaAfterReplaceParam) {
        String data = weaAfterReplaceParam.getData();
        log.writeLog("拦截报名 => " + data);
        return data;
    }

}
