package io.github.sagirikawaii01.centox.core.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
@ControllerAdvice
public class CentoxControllerAdvice implements WebMvcConfigurer {
    @Resource
    private ParamHandlerMethodArgumentResolver paramHandlerMethodArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(paramHandlerMethodArgumentResolver);
    }
}
