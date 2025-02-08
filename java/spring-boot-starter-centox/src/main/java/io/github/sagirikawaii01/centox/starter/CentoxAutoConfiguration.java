package io.github.sagirikawaii01.centox.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sagirikawaii01.centox.core.configurer.*;
import io.github.sagirikawaii01.centox.core.filter.InputStreamCacheFilter;
import io.github.sagirikawaii01.centox.core.handler.*;
import io.github.sagirikawaii01.centox.core.interceptor.ApiResponseInterceptor;
import io.github.sagirikawaii01.centox.core.interceptor.PageInterceptor;
import io.github.sagirikawaii01.centox.core.properties.PageProperties;
import io.github.sagirikawaii01.centox.log.CentoxLogAspect;
import io.github.sagirikawaii01.centox.log.CentoxLogHandler;
import io.github.sagirikawaii01.centox.log.HandledExceptionPool;
import io.github.sagirikawaii01.centox.log.LogHandler;
import io.github.sagirikawaii01.centox.x.adapter.*;
import io.github.sagirikawaii01.centox.x.exception.ApiSuccess;
import io.github.sagirikawaii01.centox.x.handler.*;
import io.github.sagirikawaii01.centox.x.util.Assert;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0
 */
@EnableConfigurationProperties(CentoxProperties.class)
@Configuration
@ConditionalOnExpression("${centox.enable:true}")
public class CentoxAutoConfiguration {
    @Resource
    private CentoxProperties properties;

    @Resource
    private ObjectMapper objectMapper;

    @Bean
    @ConditionalOnMissingBean(HandledExceptionPool.class)
    public HandledExceptionPool handledExceptionPool() {
        HandledExceptionPool pool = new HandledExceptionPool();
        pool.addHandledException(ApiSuccess.class);
        return pool;
    }

    @Bean
    @ConditionalOnMissingBean(LogHandler.class)
    public LogHandler logHandler(HandledExceptionPool handledExceptionPool) {
        return new CentoxLogHandler(handledExceptionPool);
    }

    @Bean
    @ConditionalOnMissingBean(CentoxLogAspect.class)
    public CentoxLogAspect logAspect(LogHandler logHandler, CentoxLogAspect.LogConfig logConfig) {
        return new CentoxLogAspect(logHandler, objectMapper, logConfig);
    }

    @Bean(name = "centoxLogConfig")
    @ConditionalOnMissingBean
    public CentoxLogAspect.LogConfig logConfig() {
        return new CentoxLogAspect.LogConfigBuilder()
                .setLogLength(properties.getLog().getLogLength())
                .setRunTime(properties.getLog().getRunTime())
                .setExcFullShow(properties.getLog().isExcFullShow()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiExceptionHandler apiExceptionHandler() {
        return new ApiExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiResponseHandler apiResponseHandler() {
        return new ApiResponseHandler(properties.getAnn(), properties.getResponse(), properties.getPage());
    }

    @Bean
    @ConditionalOnMissingBean
    public CentoxControllerAdvice centoxControllerAdvice() {
        return new CentoxControllerAdvice();
    }

    @Bean
    @ConditionalOnMissingBean
    public BasicErrorController basicErrorController() {
        return new CentoxErrorController();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiResponseInterceptor apiResponseInterceptor() {
        return new ApiResponseInterceptor(properties.getAnn());
    }

    @Bean
    @ConditionalOnMissingBean
    public PageInterceptor pageInterceptor() {
        return new PageInterceptor();
    }

    @Bean
    public FilterRegistrationBean<InputStreamCacheFilter> inputStreamCacheFilterFilterRegistrationBean() {
        FilterRegistrationBean<InputStreamCacheFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new InputStreamCacheFilter());
        registration.addUrlPatterns("/*");
        registration.setName("inputStreamCacheFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageConvertConfig messageConvertConfig() {
        return new MessageConvertConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public ParamHandlerMethodArgumentResolver paramHandlerMethodArgumentResolver() {
        return new ParamHandlerMethodArgumentResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public CodeTypeAdapter codeTypeAdapter() {
        return new DefaultCodeTypeAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiErrorLanguageAdapter apiErrorLanguageAdapter() {
        return new DefaultApiErrorLanguageAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public PageParameterReadConfig pageParameterReadConfig() {
        return new PageParameterReadConfig();
    }

    @Bean
    public AdapterManager adapterManager() {
        return new AdapterManager();
    }

    @Bean
    public PageProperties pageProperties() {
        PageProperties pageProperties = properties.getPage();
        Assert.notBlank(pageProperties.getRequestFields().getCurrentPage(), "Page Field Configuration Error: currentPage can not be blank.");
        Assert.notBlank(pageProperties.getRequestFields().getPageSize(), "Page Field Configuration Error: pageSize can not be blank.");
        Assert.notBlank(pageProperties.getResponseFields().getDataCount(), "Page Field Configuration Error: dataCount can not be blank.");
        Assert.notBlank(pageProperties.getResponseFields().getPageCount(), "Page Field Configuration Error: pageCount can not be blank.");
        return pageProperties;
    }



    @Bean
    @ConditionalOnMissingBean
    public DateConvertConfig dateConvertConfig() {
        return new DateConvertConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public TimeConvertConfig timeConvertConfig() {
        return new TimeConvertConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public DateTimeConvertConfig dateTimeConvertConfig() {
        return new DateTimeConvertConfig();
    }



    @Bean
    @ConditionalOnMissingBean(BaseHttpMessageNotReadableExceptionHandler.class)
    public BaseHttpMessageNotReadableExceptionHandler httpMessageNotReadableExceptionHandler() {
        return new DefaultHttpMessageNotReadableExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(BaseHttpRequestMethodNotSupportedExceptionHandler.class)
    public BaseHttpRequestMethodNotSupportedExceptionHandler httpRequestMethodNotSupportedExceptionHandler() {
        return new DefaultHttpRequestMethodNotSupportedExceptionHandler();
    }


    @Bean
    @ConditionalOnMissingBean(BaseHttpMediaTypeNotSupportedExceptionHandler.class)
    public BaseHttpMediaTypeNotSupportedExceptionHandler httpMediaTypeNotSupportedExceptionHandler() {
        return new DefaultBaseHttpMediaTypeNotSupportedExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(BaseMissingServletRequestParameterExceptionHandler.class)
    public BaseMissingServletRequestParameterExceptionHandler missingServletRequestParameterExceptionHandler() {
        return new DefaultMissingServletRequestParameterExceptionHandler();
    }

    @Bean
    @ConditionalOnClass(name = "javax.validation.ValidationException")
    @ConditionalOnMissingBean(BaseValidParamExceptionHandler.class)
    public BaseValidParamExceptionHandler validParamExceptionHandler() {
        return new DefaultValidParamExceptionHandler();
    }

    @Bean
    @ConditionalOnClass(name = "javax.validation.ValidationException")
    @ConditionalOnMissingBean(BaseValidationExceptionHandler.class)
    public BaseValidationExceptionHandler validationExceptionHandler() {
        return new DefaultValidationExceptionHandler();
    }
}
