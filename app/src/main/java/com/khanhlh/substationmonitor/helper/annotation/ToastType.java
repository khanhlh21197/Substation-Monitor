package com.khanhlh.substationmonitor.helper.annotation;

import androidx.annotation.IntDef;

/**
 * 页面描述：ToastType
 * <p>
 * Created by ditclear on 2017/10/11.
 */
@IntDef({ToastType.ERROR, ToastType.NORMAL, ToastType.SUCCESS, ToastType.WARNING})
public @interface ToastType {
    int ERROR = -2;
    int WARNING = -1;
    int NORMAL = 0;
    int SUCCESS = 1;
}
