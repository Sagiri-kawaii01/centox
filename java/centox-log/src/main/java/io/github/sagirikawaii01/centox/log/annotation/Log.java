package io.github.sagirikawaii01.centox.log.annotation;


import java.lang.annotation.*;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.1
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 超时记录时间，-1为都记录
     */
    long runTime() default -1;

    /**
     * 模块名称
     */
    String module() default "默认模块";



    /**
     * 接口名称
     */
    String apiName() default "";

    /**
     * 是否保存request数据
     */
    boolean isSaveRequestData() default false;

    /**
     * 是否保存response数据
     */
    boolean isSaveResponseData() default false;

    /**
     * 是否调用afterSaveLog
     */
    boolean saveLog() default false;
}
