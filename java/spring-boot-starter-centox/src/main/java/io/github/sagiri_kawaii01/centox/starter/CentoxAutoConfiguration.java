package io.github.sagiri_kawaii01.centox.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import io.github.sagiri_kawaii01.centox.core.configurer.*;
import io.github.sagiri_kawaii01.centox.core.filter.InputStreamCacheFilter;
import io.github.sagiri_kawaii01.centox.core.handler.*;
import io.github.sagiri_kawaii01.centox.core.interceptor.ApiResponseInterceptor;
import io.github.sagiri_kawaii01.centox.core.interceptor.PageInterceptor;
import io.github.sagiri_kawaii01.centox.core.properties.PageProperties;
import io.github.sagiri_kawaii01.centox.log.CentoxLogAspect;
import io.github.sagiri_kawaii01.centox.log.CentoxLogHandler;
import io.github.sagiri_kawaii01.centox.log.HandledExceptionPool;
import io.github.sagiri_kawaii01.centox.log.LogHandler;
import io.github.sagiri_kawaii01.centox.mp.CentoxMybatisPlusConfig;
import io.github.sagiri_kawaii01.centox.mp.DefaultPageService;
import io.github.sagiri_kawaii01.centox.mp.PageService;
import io.github.sagiri_kawaii01.centox.x.adapter.*;
import io.github.sagiri_kawaii01.centox.x.exception.ApiSuccess;
import io.github.sagiri_kawaii01.centox.x.handler.*;
import io.github.sagiri_kawaii01.centox.x.util.Assert;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

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

    @Bean(name = "ibdwLogConfig")
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
    public DateTimePatternAdapter dateTimePatternAdapter() {
        Assert.notBlank(properties.getDatetime().getTimePattern(), "DateTime Pattern Configuration Error: timePattern can not be blank.");
        Assert.notBlank(properties.getDatetime().getDatePattern(), "DateTime Pattern Configuration Error: datePattern can not be blank.");
        Assert.notBlank(properties.getDatetime().getDateTimePattern(), "DateTime Pattern Configuration Error: dateTimePattern can not be blank.");
        return new DefaultDateTimePatternAdapter(
                properties.getDatetime().getTimePattern(),
                properties.getDatetime().getDatePattern(),
                properties.getDatetime().getDateTimePattern()
        );
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
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(
            DateTimePatternAdapter dateTimePatternAdapter
    ) {
        ZoneId zoneId;
        if (!properties.getDatetime().getZoneId().isEmpty()) {
            zoneId = ZoneId.of(properties.getDatetime().getZoneId());
        } else {
            zoneId = ZoneId.systemDefault();
        }
        return builder -> builder.serializerByType(
                LocalDateTime.class, new LocalDateTimeSerializer(dateTimePatternAdapter.dateTimePattern().withZone(zoneId))
        ).serializerByType(
                LocalDate.class, new LocalDateSerializer(dateTimePatternAdapter.datePattern().withZone(zoneId))
        ).serializerByType(
                LocalTime.class, new LocalTimeSerializer(dateTimePatternAdapter.timePattern().withZone(zoneId))
        );
    }

    @Bean
    @ConditionalOnClass(name = "com.baomidou.mybatisplus.core.handlers.MetaObjectHandler")
    @ConditionalOnMissingBean
    public CentoxMybatisPlusConfig centoxMybatisPlusConfig() {
        return new CentoxMybatisPlusConfig(properties.getMp());
    }

    @Bean
    @ConditionalOnClass(name = "com.github.pagehelper.PageHelper")
    @ConditionalOnMissingBean
    public PageService pageService() {
        return new DefaultPageService();
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
