package com.customization.hxbank.oasys.service;

import com.customization.hxbank.oasys.cmd.CalendarCmd;
import com.engine.core.impl.Service;

import java.util.Map;

/**
 * @Description
 * @Author miao.zhang <yyem954135@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2020-10-13
 */
public class MyWorkPlanCalendarService extends Service {

    public Map<String, Object> getMyCalendar(Map<String, Object> params) {
        return commandExecutor.execute(new CalendarCmd(user, params));
    }
}

