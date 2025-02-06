package io.github.sagiri_kawaii01.centox.core.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD})
public @interface Pageable {
    @NotNull
    String pageSize() default "";

    @NotNull
    String currentPage() default "";

    @NotNull
    String dataCount() default "";

    @NotNull
    String pageCount() default "";

    String subBodyPath() default "";

    int limit() default 100;

    boolean enablePaginateNot() default false;

}
