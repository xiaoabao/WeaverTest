package com.api.doc.cmd;


import com.api.doc.util.DocUtil;
import com.engine.common.biz.AbstractCommonCommand;
import com.engine.common.entity.BizLogContext;
import com.engine.core.interceptor.CommandContext;
import weaver.general.BaseBean;
import weaver.hrm.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetDocListCmd extends AbstractCommonCommand<Map<String,Object>> {
    BaseBean bean = new BaseBean();
    public GetDocListCmd(Map<String,Object>paramMap, User paramUser){
        this.params=paramMap;
        this.user=paramUser;
    }
    @Override
    public Map<String, Object> execute(CommandContext commandContext) {
        HashMap<String,Object> hashMap=new HashMap<String,Object>();
        DocUtil util=new DocUtil();
        try{
            bean.writeLog("GetDocListCmd---------");
            List list=util.getFullSearch(this.params,this.user);
            if(list != null){
                hashMap.put("docs",list);
                hashMap.put("apistatus","success");
            }else{
                hashMap.put("docs",null);
                hashMap.put("apistatus","error");
            }
        }catch (Exception e){
            e.getMessage();
        }
        return    hashMap;
    }


    @Override
    public BizLogContext getLogContext() {
        return null;
    }
}
