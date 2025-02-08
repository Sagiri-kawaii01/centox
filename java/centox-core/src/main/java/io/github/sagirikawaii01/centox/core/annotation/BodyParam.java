package io.github.sagirikawaii01.centox.core.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD})
public @interface BodyParam {
    @NotNull
    String value() default "";

    @NotNull
    String defaultValue() default "";
}
