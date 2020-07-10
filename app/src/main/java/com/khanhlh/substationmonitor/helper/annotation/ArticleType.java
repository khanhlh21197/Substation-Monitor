package com.khanhlh.substationmonitor.helper.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.khanhlh.substationmonitor.helper.annotation.ArticleType.ANDROID;

/**
 * 页面描述：ArticleType 文章类型
 * <p>
 * Created by ditclear on 2017/10/17.
 */

@IntDef({ANDROID, ArticleType.PROGRAME, ArticleType.FRONT_END, ArticleType.IOS, ArticleType.DB, ArticleType.DEVLOG, ArticleType.RECOMMAND, ArticleType.DAILY})
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface ArticleType {
    int ANDROID = 16;
    int PROGRAME = 6;
    int FRONT_END = 5;
    int IOS = 27;
    int DB = 14;
    int DEVLOG = 15;
    int RECOMMAND = 32;
    int DAILY = 9;
}
