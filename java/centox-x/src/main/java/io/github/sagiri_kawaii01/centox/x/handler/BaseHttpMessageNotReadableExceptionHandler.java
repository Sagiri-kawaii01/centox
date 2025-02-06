package io.github.sagiri_kawaii01.centox.x.handler;

import io.github.sagiri_kawaii01.centox.x.pojo.ApiResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.0
 */
@ControllerAdvice
public abstract class BaseHttpMessageNotReadableExceptionHandler implements CentoxExceptionHandler {
    abstract ApiResponse<Object> handle(HttpMessageNotReadableException e);

    @Override
    public ApiResponse<Object> handle(Exception e) {
        return handle((HttpMessageNotReadableException) e);
    }
}