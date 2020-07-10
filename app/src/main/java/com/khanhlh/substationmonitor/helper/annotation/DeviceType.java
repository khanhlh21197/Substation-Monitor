package com.khanhlh.substationmonitor.helper.annotation;

import androidx.annotation.LongDef;

/**
 * 页面描述：ArticleType 文章类型
 * <p>
 * Created by ditclear on 2017/10/17.
 */

@LongDef({DeviceType.TV, DeviceType.AC, DeviceType.FAN, DeviceType.LIGHT})
public @interface DeviceType {
    long TV = 1;
    long AC = 0;
    long FAN = 2;
    long LIGHT = 3;
    long TEMP = 4;
}
