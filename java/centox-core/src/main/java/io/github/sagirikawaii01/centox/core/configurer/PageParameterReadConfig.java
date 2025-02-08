package io.github.sagirikawaii01.centox.core.configurer;

import io.github.sagirikawaii01.centox.core.interceptor.PageInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0
 */
public class PageParameterReadConfig implements WebMvcConfigurer {
    @Resource
    private PageInterceptor pageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(pageInterceptor);
    }
}
