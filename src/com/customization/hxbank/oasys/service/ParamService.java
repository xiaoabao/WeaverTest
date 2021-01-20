package com.customization.hxbank.oasys.service;

import com.customization.hxbank.oasys.entity.ParamToken;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.weaver.qfengx.DaoUtils;
import com.weaver.qfengx.RequestUtils;
import com.weaver.qfengx.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * Created by YeShengtao on 2020/10/20 17:15
 */
public class ParamService {

    public static ParamToken queryToken(String token) {
        return DaoUtils.findSqlToBean(ParamToken.class, "select * from uf_param_token where token = ?", token);
    }

    /**
     * 检查数据归档Token是否有效
     */
    public static boolean checkParamToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }

        ParamToken paramToken = DaoUtils.findSqlToBean(ParamToken.class, "select * from uf_param_token where token = ? and status = 0", token);
        if (Objects.isNull(paramToken) || StringUtils.equals(paramToken.getStatus(), "")) {
            return false;
        }
        return true;
    }

    /**
     * 检查Token的IP是否合法
     */
    public static boolean checkParamTokenIp(ParamToken token, HttpServletRequest request) {
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
