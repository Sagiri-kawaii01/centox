package io.github.sagirikawaii01.centox.x.handler;


import io.github.sagirikawaii01.centox.x.pojo.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.0
 */

@ControllerAdvice
public class DefaultValidParamExceptionHandler extends BaseValidParamExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(DefaultValidParamExceptionHandler.class);
    @Override
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseBody
    public ApiResponse<Object> handle(BindException e) {
        List<FieldError> fieldErrors = e.getFieldErrors();
        Map<String, String> error = new HashMap<>();
        fieldErrors.forEach((ex)-> error.put(ex.getField(), ex.getDefaultMessage()));
        return new ApiResponse<>(90006, "参数验证异常", error);
    }


}