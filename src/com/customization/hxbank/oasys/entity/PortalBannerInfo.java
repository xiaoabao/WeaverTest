package com.customization.hxbank.oasys.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Created by YeShengtao on 2020/10/29 10:32
 */
@Data
@Accessors(chain = true)
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PortalBannerInfo {

    private String targetUrl = "";
    private String bannerImage = "";

    public String getTargetUrl() {
        return targetUrl;
    }

    public PortalBannerInfo setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
        return this;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public PortalBannerInfo setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
        return this;
    }
}
