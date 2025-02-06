package io.github.sagiri_kawaii01.centox.x.handler;

import io.github.sagiri_kawaii01.centox.x.pojo.ApiResponse;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.1.2
 */
@ControllerAdvice
public abstract class BaseMissingServletRequestParameterExceptionHandler implements CentoxExceptionHandler {
    abstract ApiResponse<Object> handle(MissingServletRequestParameterException e);

    @Override
    public ApiResponse<Object> handle(Exception e) {
        return handle((MissingServletRequestParameterException) e);
    }
}
