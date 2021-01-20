package com.api.doc.service;

import weaver.hrm.User;

import java.util.Map;

public interface NoticeService {
    Map<String,Object> getDocList(Map<String,Object> paramMap, User paramUser);
}
