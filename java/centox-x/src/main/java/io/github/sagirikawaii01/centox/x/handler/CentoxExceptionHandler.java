package io.github.sagirikawaii01.centox.x.handler;


import io.github.sagirikawaii01.centox.x.pojo.ApiResponse;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.0
 */
public interface CentoxExceptionHandler {
    ApiResponse<Object> handle(Exception e);
}
