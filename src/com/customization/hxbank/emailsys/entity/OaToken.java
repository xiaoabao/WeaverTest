package com.customization.hxbank.emailsys.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 综合门户接口对接令牌
 * Created by YeShengtao on 2020/10/18 16:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class OaToken {
    private String systemName;
    private String token;
    private String remark;
    private String ip;
    private String status;

    public String getSystemName() {
        return systemName;
    }

    public OaToken setSystemName(String systemName) {
        this.systemName = systemName;
        return this;
    }

    public String getToken() {
        return token;
    }

    public OaToken setToken(String token) {
        this.token = token;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public OaToken setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public OaToken setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public OaToken setStatus(String status) {
        this.status = status;
        return this;
    }
}
