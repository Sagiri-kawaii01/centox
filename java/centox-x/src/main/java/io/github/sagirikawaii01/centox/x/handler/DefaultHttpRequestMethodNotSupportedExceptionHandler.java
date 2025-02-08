package io.github.sagirikawaii01.centox.x.handler;


import io.github.sagirikawaii01.centox.x.pojo.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.0
 */

@ControllerAdvice
public class DefaultHttpRequestMethodNotSupportedExceptionHandler extends BaseHttpRequestMethodNotSupportedExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(DefaultHttpRequestMethodNotSupportedExceptionHandler.class);

    @Override
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ApiResponse<Object> handle(HttpRequestMethodNotSupportedException e) {
        log.warn(e.getMessage());
        if (null == e.getSupportedMethods()) {
            return new ApiResponse<>(405, "不支持的请求方式:" + e.getMethod());
        }
        return new ApiResponse<>(405, "不支持的请求方式:" + e.getMethod() + ", 请使用[" + String.join(", ", e.getSupportedMethods()) + "]");
    }
}