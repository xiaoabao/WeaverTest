package com.customization.hxbank.oasys.service;

import com.customization.hxbank.oasys.entity.OaToken;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.weaver.qfengx.DaoUtils;
import com.weaver.qfengx.RequestUtils;
import com.weaver.qfengx.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * OA门户接口服务类
 * Created by YeShengtao on 2020/9/24 16:59
 */
public class OaDoorService {

    public static OaToken queryToken(String token) {
        return DaoUtils.findSqlToBean(OaToken.class, "select * from uf_oa_token where token = ?", token);
    }

    /**
     * 检查数据归档Token是否有效
     */
    public static boolean checkOaToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }

        OaToken oaToken = DaoUtils.findSqlToBean(OaToken.class, "select * from uf_oa_token where token = ? and status = 0", token);
        if (Objects.isNull(oaToken) || StringUtils.equals(oaToken.getStatus(), "")) {
            return false;
        }
        return true;
    }

    /**
     * 检查Token的IP是否合法
     */
    public static boolean checkOaTokenIp(OaToken token, HttpServletRequest request) {
        String ips = token.getIp();
        if (StringUtils.isBlank(ips)) {
            return true;
        }
        List<String> ipList = Lists.newArrayList(Splitter.on(",").trimResults().omitEmptyStrings().split(ips));
        if (ipList.contains(RequestUtils.ip(request))) {
            return true;
        }
        return false;
    }

}
