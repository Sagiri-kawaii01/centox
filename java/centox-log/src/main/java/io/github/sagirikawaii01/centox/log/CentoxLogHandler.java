package io.github.sagirikawaii01.centox.log;

import io.github.sagirikawaii01.centox.log.annotation.ApiName;
import io.github.sagirikawaii01.centox.log.annotation.Log;
import io.github.sagirikawaii01.centox.log.annotation.SaveRequest;
import io.github.sagirikawaii01.centox.log.annotation.SaveResponse;
import io.github.sagirikawaii01.centox.log.pojo.LogData;
import io.github.sagirikawaii01.centox.log.pojo.LogHandlerConfig;
import io.github.sagirikawaii01.centox.log.pojo.RequestStatus;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.1
 */
public class CentoxLogHandler implements LogHandler {

    private static final Logger log = LoggerFactory.getLogger(CentoxLogHandler.class);

    protected HandledExceptionPool pool;

    public CentoxLogHandler(HandledExceptionPool pool) {
        this.pool = pool;
    }

    @Override
    public boolean beforeSaveLog(ProceedingJoinPoint pjp) {
        return true;
    }

    @Override
    public void afterSaveLog(LogData logData) {

    }

    @Override
    public void setExtraDataFromRequest(HttpServletRequest request, LogData logData) {

    }

    @Override
    public LogData handler(JoinPoint jp, Object keys, Throwable e, LogHandlerConfig config) {
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        assert request != null;
        // 输出日志VO
        LogData logData = new LogData();
        try {
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) jp.getSignature();
            // 获取切入点所在的方法
            Method method = signature.getMethod();
            boolean saveRequest = null != method.getAnnotation(SaveRequest.class);
            boolean saveResponse = null != method.getAnnotation(SaveResponse.class);
            // 获取注解信息
            copyAnnotationValue(method, logData);
            saveClassName(logData, jp);
            saveMethodName(logData, method);
            saveUri(logData, request);
            saveIp(logData, request);
            //操作时间点
            logData.setReqTime(LocalDateTime.now());

            //异常名称+异常信息
            if(null != e){
                if (pool.exist(e.getClass())) {
                    config.setExcFullShow(false);
                }
                logData.setExcName(e.getClass().getName());
                logData.setExcInfo(stackTraceToString(e.getClass().getName(), e.getMessage(), e.getStackTrace(), config));
            }
            if (config.isSaveRequestData() || saveRequest) {
                //请求的参数，参数所在的数组转换成json
                logData.setParams(Arrays.toString(jp.getArgs()));
            }
            if (config.isSaveResponseData() || saveResponse) {
                //返回值
                if(null != keys && Void.class.getName() != keys){
                    StringBuilder result = new StringBuilder(config.getObjectMapper().writeValueAsString(keys));
                    if (config.getResultLength() == 0){
                        //表示全部
                        logData.setReturnValue(result.toString());
                    } else {
                        String tempResult = result.substring(0, config.getResultLength());
                        logData.setReturnValue(tempResult);
                    }
                }
            }
            setExtraDataFromRequest(request, logData);
        } catch (Exception ignored) {

        }
        doLog(e, logData, config);
        return logData;
    }

    protected void saveClassName(LogData logData, JoinPoint jp) {
        // 获取请求的类名
        logData.setClassName(jp.getTarget().getClass().getName());
    }

    protected void saveMethodName(LogData logData, Method method) {
        // 获取请求的方法名
        logData.setMethodName(method.getName());
    }

    protected void saveUri(LogData logData, HttpServletRequest request) {
        //请求uri
        logData.setUri(request.getRequestURI());
    }

    protected void saveIp(LogData logData, HttpServletRequest request) {
        // 请求ip
        logData.setIp(getIpAddr(request));
    }

    protected void copyAnnotationValue(Method method, LogData logData) {
        // 获取注解信息
        Log anno = method.getAnnotation(Log.class);
        ApiName apiName = method.getAnnotation(ApiName.class);
        SaveRequest saveRequest = method.getAnnotation(SaveRequest.class);
        SaveResponse saveResponse = method.getAnnotation(SaveResponse.class);
        if (anno == null) {
            anno = method.getDeclaringClass().getAnnotation(Log.class);
        }

        logData.setModule(anno.module());
        logData.setApiName(anno.apiName());
        logData.setSaveRequestData(anno.isSaveRequestData());
        logData.setSaveResponseData(anno.isSaveResponseData());
        logData.setStatus(RequestStatus.Success);

        if (apiName != null) {
            logData.setApiName(apiName.value());
        }
        if (saveRequest != null) {
            logData.setSaveRequestData(true);
        }
        if (saveResponse != null) {
            logData.setSaveResponseData(true);
        }
    }

    protected boolean isFailed(Throwable e) {
        return !pool.exist(e.getClass());
    }

    protected void doLog(Throwable e, LogData logData, LogHandlerConfig handlerConfig) {
        if (null == e) {
            logData.setExecTime(handlerConfig.getEndAt() - handlerConfig.getStartAt());
            log.info(logData.toString());
        } else {
            if (isFailed(e)) {
                logData.setStatus(RequestStatus.Fail);
            } else {
                logData.setExecTime(handlerConfig.getEndAt() - handlerConfig.getStartAt());
                log.info(logData.toString());
                return;
            }
            if (pool.exist(e.getClass())) {
                logData.setExecTime(handlerConfig.getEndAt() - handlerConfig.getStartAt());
                log.warn(logData.toString());
                return;
            }
            logData.setExecTime(-1L);
            log.error(logData.toString());
        }
    }

    protected String stackTraceToString(String exceptionName, String exceptionMessage, StackTraceElement[] elements, LogHandlerConfig handlerConfig) {
        StringBuilder builder = new StringBuilder();
        if (handlerConfig.getExcFullShow()) {
            for (StackTraceElement stet : elements) {
                builder.append(stet).append("\n");
            }
            return exceptionName + ":" + exceptionMessage + "\n\t" + builder;
        }
        return exceptionName + ":" + exceptionMessage;
    }

    /**
     * 获取访问者的ip地址
     * 注：要外网访问才能获取到外网地址，如果你在局域网甚至本机上访问，获得的是内网或者本机的ip
     */
    protected static String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            //X-Forwarded-For：Squid 服务代理
            String ipAddresses = request.getHeader("X-Forwarded-For");

            if (ipAddresses == null || ipAddresses.isEmpty() ||
                    "unknown".equalsIgnoreCase(ipAddresses)) {
                //Proxy-Client-IP：apache 服务代理
                ipAddresses = request.getHeader("Proxy-Client-IP");
            }

            if (ipAddresses == null || ipAddresses.isEmpty() ||
                    "unknown".equalsIgnoreCase(ipAddresses)) {
                //WL-Proxy-Client-IP：weblogic 服务代理
                ipAddresses = request.getHeader("WL-Proxy-Client-IP");
            }

            if (ipAddresses == null || ipAddresses.isEmpty() ||
                    "unknown".equalsIgnoreCase(ipAddresses)) {
                //HTTP_CLIENT_IP：有些代理服务器
                ipAddresses = request.getHeader("HTTP_CLIENT_IP");
            }

            if (ipAddresses == null || ipAddresses.isEmpty() ||
                    "unknown".equalsIgnoreCase(ipAddresses)) {
                //X-Real-IP：nginx服务代理
                ipAddresses = request.getHeader("X-Real-IP");
            }

            //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
            if (ipAddresses != null && !ipAddresses.isEmpty()) {
                ipAddress = ipAddresses.split(",")[0];
            }

            //还是不能获取到，最后再通过request.getRemoteAddr();获取
            if (ipAddress == null || ipAddress.isEmpty() ||
                    "unknown".equalsIgnoreCase(ipAddresses)) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            ipAddress = "";
        }
        return ipAddress;
    }

}
