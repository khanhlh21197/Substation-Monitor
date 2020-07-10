package com.khanhlh.substationmonitor.helper.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 页面描述：ArticleType 文章类型
 * <p>
 * Created by ditclear on 2017/10/17.
 */

@IntDef({ItemType.HEADER, ItemType.ITEM, ItemType.FOOTER})
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface ItemType {
    int HEADER = 1;
    int ITEM = 2;
    int FOOTER = 3;
}
