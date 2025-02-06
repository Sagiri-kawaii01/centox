package io.github.sagiri_kawaii01.centox.core.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import io.github.sagiri_kawaii01.centox.core.ApiError;
import io.github.sagiri_kawaii01.centox.core.annotation.BodyParam;
import io.github.sagiri_kawaii01.centox.core.annotation.Pageable;
import io.github.sagiri_kawaii01.centox.core.properties.PageProperties;
import io.github.sagiri_kawaii01.centox.core.util.RequestUtil;
import io.github.sagiri_kawaii01.centox.x.adapter.AdapterManager;
import io.github.sagiri_kawaii01.centox.x.adapter.DateTimePatternAdapter;
import io.github.sagiri_kawaii01.centox.x.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class ParamHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    private final Logger log = LoggerFactory.getLogger(ParamHandlerMethodArgumentResolver.class);

    @Resource
    private PageProperties pageProperties;

    @Resource
    private DateTimePatternAdapter dateTimePatternAdapter;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return null != parameter.getParameterAnnotation(BodyParam.class) ||
                null != parameter.getMethodAnnotation(Pageable.class) ||
                null != parameter.getMethodAnnotation(BodyParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer, @NotNull NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        if (null != parameter.getParameterAnnotation(BodyParam.class)) {
            return bodyParamHandler(parameter, nativeWebRequest);
        } else if (null != parameter.getMethodAnnotation(Pageable.class)) {
            return pageableHandler(parameter, nativeWebRequest);
        } else if (null != parameter.getMethodAnnotation(BodyParam.class)) {
            return bodyParamHandler(parameter, nativeWebRequest);
        }
        return null;
    }

    private Object pageableHandler(MethodParameter parameter, NativeWebRequest nativeWebRequest) throws Exception {
        String paramName = parameter.getParameterName();
        Pageable anno = parameter.getMethodAnnotation(Pageable.class);
        assert anno != null;
        String subBodyPath = anno.subBodyPath().isEmpty() ? pageProperties.getRequestFields().getSubBodyPath() : anno.subBodyPath();
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        assert request != null;

        JSONObject jsonObject = RequestUtil.getJsonObject((ContentCachingRequestWrapper) request);
        Class<?> parameterType = parameter.getParameterType();
        Type genericType = parameter.getGenericParameterType();

        if ("GET".equals(request.getMethod())) {
            if (!List.class.isAssignableFrom(parameterType)) {
                String p = request.getParameter(paramName);
                if (parameterType != String.class && p.isEmpty()) {
                    return null;
                }
                if (parameterType == String.class) {
                    return p;
                }
                try {
                    if (parameterType == Integer.class) {
                        return Integer.parseInt(p);
                    }
                    if (parameterType == Double.class) {
                        return Double.parseDouble(p);
                    }
                    if (parameterType == Long.class) {
                        return Long.parseLong(p);
                    }
                    if (parameterType == Float.class) {
                        return Float.parseFloat(p);
                    }
                    if (parameterType == Boolean.class) {
                        return Boolean.parseBoolean(p);
                    }
                    if (parameterType == LocalDateTime.class) {
                        return LocalDateTime.parse(p, dateTimePatternAdapter.dateTimePattern());
                    }
                    if (parameterType == LocalDate.class) {
                        return LocalDate.parse(p, dateTimePatternAdapter.datePattern());
                    }
                    if (parameterType == LocalTime.class) {
                        return LocalTime.parse(p, dateTimePatternAdapter.timePattern());
                    }
                } catch (NumberFormatException | DateTimeParseException e2) {
                    throw new ApiException((int) ApiError.PARAMS_TYPE_ERROR.code(), AdapterManager.instance.translate(ApiError.PARAMS_TYPE_ERROR.message(), null));
                }
                return p;
            } else {
                if (genericType instanceof ParameterizedType) {
                    return JSON.parseArray(JSON.toJSONString(request.getParameterValues(paramName))).toJavaList(Class.forName(((ParameterizedType) genericType).getActualTypeArguments()[0].getTypeName()));
                }
            }
        }
        Object object = null;

        if (!subBodyPath.isEmpty()) {
            JSONObject subBody = jsonObject;
            try {
                for (String key : subBodyPath.split("\\.")) {
                    subBody = subBody.getJSONObject(key);
                }
            } catch (JSONException e) {
                throw new ApiException((int) ApiError.PARAMS_READ_ERROR.code(), AdapterManager.instance.translate(ApiError.PARAMS_READ_ERROR.message(), null));
            }
            if (null != subBody && !subBody.containsKey(paramName) && !jsonObject.containsKey(paramName)) {
                return getDefaultValue(parameterType);
            }
            if (null != subBody) {
                object = getObject(paramName, subBody, parameterType, genericType);
            }
        }

        if (null == object) {
            object = getObject(paramName, jsonObject, parameterType, genericType);
        }
        return object;
    }

    private Object getDefaultValue(Class<?> parameterType) {
        switch (parameterType.getName()) {
            case "int":
            case "short":
            case "long":
            case "char":
                return 0;
            case "float":
            case "double":
                return 0.0;
            case "boolean":
                return false;
        }
        return null;
    }

    private Object getObject(String paramName, JSONObject jsonObject, Class<?> parameterType, Type genericType) {
        if (parameterType.equals(String.class)) {
            return jsonObject.getString(paramName);
        }
        try {
            if (parameterType.equals(Long.class)) {
                return jsonObject.getLong(paramName);
            }
            if (parameterType.equals(Integer.class)) {
                return jsonObject.getInteger(paramName);
            }
            if (parameterType.equals(Double.class)) {
                return jsonObject.getDouble(paramName);
            }
            if (parameterType.equals(Boolean.class)) {
                return jsonObject.getBoolean(paramName);
            }
            if (parameterType.equals(BigDecimal.class)) {
                return jsonObject.getBigDecimal(paramName);
            }
            if (parameterType.equals(BigInteger.class)) {
                return jsonObject.getBigInteger(paramName);
            }
            if (List.class.isAssignableFrom(parameterType) && genericType instanceof ParameterizedType) {
                JSONArray array = jsonObject.getJSONArray(paramName);
                if (null == array) {
                    return null;
                }
                return array.toJavaList(Class.forName(((ParameterizedType) genericType).getActualTypeArguments()[0].getTypeName()));
            }
            return jsonObject.getObject(paramName, parameterType);
        } catch (NumberFormatException ignored) {
            throw new ApiException((int) ApiError.PARAMS_TYPE_ERROR.code(), AdapterManager.instance.translate(ApiError.PARAMS_TYPE_ERROR.message(), null));
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            throw new ApiException((int) ApiError.PARAMS_READ_ERROR.code(), AdapterManager.instance.translate(ApiError.PARAMS_READ_ERROR.message(), null));
        }
    }

    private Object bodyParamHandler(MethodParameter parameter, NativeWebRequest nativeWebRequest) throws Exception {
        BodyParam anno = parameter.getParameterAnnotation(BodyParam.class);
        String value = null == anno ? null : anno.value();
        if (null == value || value.isEmpty()) {
            value = parameter.getParameterName();
        }
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        assert request != null;
        Class<?> parameterType = parameter.getParameterType();
        JSONObject jsonObject = RequestUtil.getJsonObject((ContentCachingRequestWrapper) request);
        String defaultValue = null == anno ? null : anno.defaultValue();
        if (!jsonObject.containsKey(value)) {
            if (null == defaultValue || defaultValue.isEmpty()) {
                return getDefaultValue(parameterType);
            } else {
                jsonObject = new JSONObject();
                jsonObject.put(value, defaultValue);
            }
        }
        return getObject(value, jsonObject, parameterType, parameter.getGenericParameterType());
    }
}
