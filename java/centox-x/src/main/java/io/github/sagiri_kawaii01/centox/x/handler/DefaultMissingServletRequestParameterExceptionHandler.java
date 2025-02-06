package io.github.sagiri_kawaii01.centox.x.handler;

import io.github.sagiri_kawaii01.centox.x.pojo.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.1.2
 */
@ControllerAdvice
public class DefaultMissingServletRequestParameterExceptionHandler extends BaseMissingServletRequestParameterExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(DefaultBaseHttpMediaTypeNotSupportedExceptionHandler.class);

    @Override
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ApiResponse<Object> handle(MissingServletRequestParameterException e) {
        log.warn(e.getMessage());
        return new ApiResponse<>(90006, "参数验证异常", "缺少必填参数：" + e.getParameterName());
    }
}
