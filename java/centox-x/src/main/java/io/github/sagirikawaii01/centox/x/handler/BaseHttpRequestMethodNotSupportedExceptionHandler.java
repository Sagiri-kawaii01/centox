package io.github.sagirikawaii01.centox.x.handler;

import io.github.sagirikawaii01.centox.x.pojo.ApiResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.0
 */
@ControllerAdvice
public abstract class BaseHttpRequestMethodNotSupportedExceptionHandler implements CentoxExceptionHandler {
    abstract ApiResponse<Object> handle(HttpRequestMethodNotSupportedException e);

    @Override
    public ApiResponse<Object> handle(Exception e) {
        return handle((HttpRequestMethodNotSupportedException) e);
    }
}