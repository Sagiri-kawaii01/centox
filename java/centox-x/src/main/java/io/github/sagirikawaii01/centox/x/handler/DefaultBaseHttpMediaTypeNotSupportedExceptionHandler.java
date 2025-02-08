package io.github.sagirikawaii01.centox.x.handler;


import io.github.sagirikawaii01.centox.x.pojo.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Iterator;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.0
 */
@ControllerAdvice
public class DefaultBaseHttpMediaTypeNotSupportedExceptionHandler extends BaseHttpMediaTypeNotSupportedExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(DefaultBaseHttpMediaTypeNotSupportedExceptionHandler.class);

    @Override
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    public ApiResponse<Object> handle(HttpMediaTypeNotSupportedException e) {
        log.warn(e.getMessage());
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        Iterator<MediaType> iterator = e.getSupportedMediaTypes().iterator();
        while (iterator.hasNext()) {
            MediaType next = iterator.next();
            builder.append(next.getType()).append('/').append(next.getSubtype());
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append(']');
        if (e.getContentType() == null) {
            return new ApiResponse<>(415, "不支持的参数类型");
        }
        return new ApiResponse<>(415, "不支持的参数类型:" + e.getContentType().getType() + "/" + e.getContentType().getSubtype() + ", 支持的参数类型:" + builder);
    }
}