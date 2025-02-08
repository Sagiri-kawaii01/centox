package io.github.sagirikawaii01.centox.x.handler;


import io.github.sagirikawaii01.centox.x.pojo.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.0
 */

@ControllerAdvice
public class DefaultHttpMessageNotReadableExceptionHandler extends BaseHttpMessageNotReadableExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(DefaultHttpMessageNotReadableExceptionHandler.class);

    @Override
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    @ResponseBody
    public ApiResponse<Object> handle(HttpMessageNotReadableException e) {
        log.warn(e.getMessage());
        return new ApiResponse<>(40004, "Json read error, check your json format.");
    }
}