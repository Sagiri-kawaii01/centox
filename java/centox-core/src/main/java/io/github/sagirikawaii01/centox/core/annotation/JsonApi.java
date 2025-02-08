package io.github.sagirikawaii01.centox.core.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface JsonApi {
    @NotNull
    String code() default "";

    @NotNull
    String message() default "";

    @NotNull
    String data() default "";
}
