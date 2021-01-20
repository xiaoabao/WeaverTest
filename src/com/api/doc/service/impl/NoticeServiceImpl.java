package com.api.doc.service.impl;

import com.api.doc.cmd.GetDocListCmd;
import com.api.doc.service.NoticeService;
import com.engine.core.impl.Service;
import com.engine.core.interceptor.Command;
import weaver.hrm.User;

import java.util.Map;

public class NoticeServiceImpl extends Service implements NoticeService {
    @Override
    public Map<String, Object> getDocList(Map<String, Object> paramMap, User paramUser) {
        return (Map<String,Object>)this.commandExecutor.execute((Command) new GetDocListCmd(paramMap,paramUser));
    }
}
