package com.customization.hxbank.oasys.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Created by YeShengtao on 2020/9/25 17:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class PrtcToken {

    private String id;
    private String token;
    private String remark;
    private String status;

    public String getId() {
        return id;
    }

    public PrtcToken setId(String id) {
        this.id = id;
        return this;
    }

    public String getToken() {
        return token;
    }

    public PrtcToken setToken(String token) {
        this.token = token;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public PrtcToken setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public PrtcToken setStatus(String status) {
        this.status = status;
        return this;
    }
}
