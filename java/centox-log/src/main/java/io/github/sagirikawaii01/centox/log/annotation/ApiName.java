package io.github.sagirikawaii01.centox.log.annotation;

import java.lang.annotation.*;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.7
 * 用来记录日志
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiName {
    String value();
}
