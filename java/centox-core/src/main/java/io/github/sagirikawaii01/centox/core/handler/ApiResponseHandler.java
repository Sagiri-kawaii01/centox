package io.github.sagirikawaii01.centox.core.handler;

import io.github.sagirikawaii01.centox.core.ApiError;
import io.github.sagirikawaii01.centox.core.NoPackagingRequired;
import io.github.sagirikawaii01.centox.core.annotation.JsonApi;
import io.github.sagirikawaii01.centox.core.annotation.Pageable;
import io.github.sagirikawaii01.centox.core.properties.ApiResponseProperties;
import io.github.sagirikawaii01.centox.core.properties.PageProperties;
import io.github.sagirikawaii01.centox.core.store.PageStore;
import io.github.sagirikawaii01.centox.x.adapter.AdapterManager;
import io.github.sagirikawaii01.centox.x.pojo.ApiResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
@ControllerAdvice
public class ApiResponseHandler implements ResponseBodyAdvice<Object> {
    private final String ANN;
    private final Logger log = LoggerFactory.getLogger(ApiResponseHandler.class);
    private final ApiResponseProperties properties;
    private final PageProperties pageProperties;

    public ApiResponseHandler(@NotNull ApiResponseProperties properties, @NotNull PageProperties pageProperties) {
        this("Json-Api-Resposne", properties, pageProperties);
    }

    public ApiResponseHandler(@NotNull String ann, @NotNull ApiResponseProperties properties, @NotNull PageProperties pageProperties) {
        this.ANN = ann;
        this.properties = properties;
        this.pageProperties = pageProperties;
    }

    @Override
    public boolean supports(@NotNull MethodParameter methodParameter, @NotNull Class<? extends HttpMessageConverter<?>> aClass) {
        ServletRequestAttributes sra = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        assert sra != null;
        HttpServletRequest request = sra.getRequest();
        JsonApi jsonApi = (JsonApi) request.getAttribute(ANN);
        return jsonApi != null;
    }

    @Override
    public Object beforeBodyWrite(Object o, @NotNull MethodParameter methodParameter, @NotNull MediaType mediaType, @NotNull Class<? extends HttpMessageConverter<?>> aClass, @NotNull ServerHttpRequest serverHttpRequest, @NotNull ServerHttpResponse serverHttpResponse) {
        if (o instanceof ApiResponse || o instanceof NoPackagingRequired) {
            return o instanceof NoPackagingRequired ? o : createResponseBody((ApiResponse<?>) o);
        } else if (o instanceof LinkedHashMap) {
            LinkedHashMap<?, ?> r = (LinkedHashMap<?, ?>) o;
            if (r.containsKey("status") && r.containsKey("error")) {
                log.error(r.toString());
                return createResponseBody(ApiResponse.fail((int)ApiError.SYSTEM_ERROR.code(), AdapterManager.instance.translate(ApiError.SYSTEM_ERROR.message(), null)));
            }
        }
        if (null != methodParameter.getMethodAnnotation(Pageable.class) && 0 != PageStore.getCurrentPage()) {
            Pageable anno = methodParameter.getMethodAnnotation(Pageable.class);
            assert anno != null;
            String currentPageKey = anno.currentPage().isEmpty() ? pageProperties.getRequestFields().getCurrentPage() : anno.currentPage();
            String pageSizeKey = anno.pageSize().isEmpty() ? pageProperties.getRequestFields().getPageSize(): anno.pageSize();
            String dataCountKey = anno.dataCount().isEmpty() ? pageProperties.getResponseFields().getDataCount() : anno.dataCount();
            String pageCountKey = anno.pageCount().isEmpty() ? pageProperties.getResponseFields().getPageCount() : anno.pageCount();
            Map<String, Object> map = new HashMap<>();
            map.put(currentPageKey, PageStore.getCurrentPage());
            map.put(pageSizeKey, PageStore.getPageSize());
            map.put(dataCountKey, PageStore.getDataCount());
            map.put(pageCountKey, PageStore.getPageCount());
            map.put(properties.getDataName(), o);
            return ApiResponse.ok(map);
        }
        return createResponseBody(ApiResponse.ok(o));
    }

    private Object createResponseBody(ApiResponse<?> result) {
        Map<String, Object> body = new HashMap<>();
        body.put(properties.getCodeName(), result.getCode());
        body.put(properties.getMessageName(), result.getMessage());
        body.put(properties.getDataName(), result.getData());
        return body;
    }
}
