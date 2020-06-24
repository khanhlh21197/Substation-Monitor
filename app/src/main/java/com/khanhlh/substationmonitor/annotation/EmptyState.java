package com.khanhlh.substationmonitor.annotation;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 页面描述：ToastType
 * <p>
 * Created by ditclear on 2017/10/11.
 */
@IntDef({EmptyState.NORMAL, EmptyState.EMPTY_ARTICLE, EmptyState.EMPTY_CODE, EmptyState.EMPTY_SEARCH, EmptyState.EMPTY_COLLECT})
@Retention(RetentionPolicy.SOURCE)
public @interface EmptyState {

    int NORMAL = 0;
    int EMPTY_ARTICLE = 1;
    int EMPTY_CODE = 2;
    int EMPTY_SEARCH = 3;
    int EMPTY_COLLECT = 4;

}
