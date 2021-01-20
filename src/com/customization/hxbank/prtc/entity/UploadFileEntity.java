package com.customization.hxbank.prtc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.File;

/**
 * 文件上传包装实体类
 * Created by YeShengtao on 2020/9/27 22:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class UploadFileEntity {

    private File file;
    private String fieldname;
    private String user;
    private String requestid;

}
