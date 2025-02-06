package io.github.sagiri_kawaii01.centox.core.handler;

import io.github.sagiri_kawaii01.centox.x.exception.ApiException;
import io.github.sagiri_kawaii01.centox.x.pojo.ApiResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
@ControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(value = ApiException.class)
    @ResponseBody
    public ApiResponse<Object> error(ApiException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }
}
