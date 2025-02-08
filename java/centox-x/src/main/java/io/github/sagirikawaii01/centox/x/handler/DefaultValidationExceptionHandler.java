package io.github.sagirikawaii01.centox.x.handler;

import io.github.sagirikawaii01.centox.x.pojo.ApiResponse;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.0
 */
@ControllerAdvice
public class DefaultValidationExceptionHandler extends BaseValidationExceptionHandler{


    @Override
    @ExceptionHandler({ValidationException.class, ConstraintViolationException.class})
    @ResponseBody
    public ApiResponse<Object> handle(ValidationException e) {
        if (e instanceof ConstraintViolationException) {
            Map<String, String> msgMap = new HashMap<>();
            ((ConstraintViolationException) e).getConstraintViolations().forEach(o-> msgMap.put(((PathImpl )o.getPropertyPath()).getLeafNode().asString(), o.getMessage()));
            return new ApiResponse<>(90006, "参数验证异常", msgMap);
        }
        return new ApiResponse<>(90006, e.getMessage());
    }
}
