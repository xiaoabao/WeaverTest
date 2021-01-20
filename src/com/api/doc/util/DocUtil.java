package com.api.doc.util;

import weaver.general.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.hrm.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocUtil {

    public List<Map<String,Object>> getFullSearch(Map paramMap,User paramUser) {
        new BaseBean().writeLog("DocUtil------getFullSearch");
        List<Map<String,Object>> arrayList=new ArrayList<Map<String,Object>>();
        RecordSet rs = new RecordSet();
        String sql="select * from docdetail";
        rs.execute(sql);
        Map<String,Object> res = new HashMap<String,Object>();
        while (rs.next()){
            res.put("docsubject", Util.null2String(rs.getString("docsubject")));
            res.put("doctype", Util.null2String(rs.getString("doctype")));
            arrayList.add(res);
        }
        return arrayList;
    }
}
