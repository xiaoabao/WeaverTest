package com.customization.hxbank.oasys.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Created by YeShengtao on 2020/10/20 17:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class ParamToken {
    private String systemName;
    private String token;
    private String remark;
    private String ip;
    private String status;

    public String getSystemName() {
        return systemName;
    }

    public ParamToken setSystemName(String systemName) {
        this.systemName = systemName;
        return this;
    }

    public String getToken() {
        return token;
    }

    public ParamToken setToken(String token) {
        this.token = token;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public ParamToken setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public ParamToken setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ParamToken setStatus(String status) {
        this.status = status;
        return this;
    }
}

