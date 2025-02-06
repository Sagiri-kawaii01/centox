package io.github.sagiri_kawaii01.centox.core.interceptor;

import io.github.sagiri_kawaii01.centox.core.annotation.JsonApi;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class ApiResponseInterceptor implements HandlerInterceptor {
    private final String ANN;

    public ApiResponseInterceptor() {
        this("Json-Api-Response");
    }

    public ApiResponseInterceptor(String ann) {
        this.ANN = ann;
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        final HandlerMethod handlerMethod = (HandlerMethod) handler;
        final Class<?> clazz = handlerMethod.getBeanType();
        final Method method = handlerMethod.getMethod();
        if (clazz.isAnnotationPresent(JsonApi.class)) {
            request.setAttribute(ANN, clazz.getAnnotation(JsonApi.class));
        } else if (method.isAnnotationPresent(JsonApi.class)) {
            request.setAttribute(ANN, method.getAnnotation(JsonApi.class));
        }
        return true;
    }
}
