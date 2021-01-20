package com.customization.hxbank.oasys.service;

import com.customization.hxbank.oasys.cmd.QuickLookCmd;
import com.engine.core.impl.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @Author miao.zhang <yyem954135@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2020-09-27
 */
public class QuickLookService extends Service {

    public Map<String, Object> getDocInfoList(Map<String, Object> params) {

        Map<String, Object> apidatas = new HashMap<String, Object>();
        if (null == user) {
            apidatas.put("hasRight", false);
            return apidatas;
        }
        return commandExecutor.execute(new QuickLookCmd(user, params));
    }
}

