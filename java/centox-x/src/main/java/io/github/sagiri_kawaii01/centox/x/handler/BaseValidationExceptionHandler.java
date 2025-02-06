package io.github.sagiri_kawaii01.centox.x.handler;

import io.github.sagiri_kawaii01.centox.x.pojo.ApiResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.validation.ValidationException;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.0
 */
@ControllerAdvice
public abstract class BaseValidationExceptionHandler implements CentoxExceptionHandler {
    abstract ApiResponse<Object> handle(ValidationException e);

    @Override
    public ApiResponse<Object> handle(Exception e) {
        return handle((ValidationException) e);
    }
}
