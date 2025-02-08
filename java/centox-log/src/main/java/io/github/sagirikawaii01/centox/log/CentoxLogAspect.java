package io.github.sagirikawaii01.centox.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sagirikawaii01.centox.log.annotation.Log;
import io.github.sagirikawaii01.centox.log.annotation.SaveLog;
import io.github.sagirikawaii01.centox.log.pojo.LogData;
import io.github.sagirikawaii01.centox.log.pojo.LogHandlerConfig;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.1
 */
@Aspect
public class CentoxLogAspect {

    private final LogHandler logHandler;

    private final ObjectMapper objectMapper;

    private final LogConfig logConfig;


    public CentoxLogAspect(LogHandler logHandler, ObjectMapper objectMapper, LogConfig logConfig) {
        this.logHandler = logHandler;
        this.objectMapper = objectMapper;
        this.logConfig = logConfig;
    }


    @Pointcut(value = "(@within(io.github.sagirikawaii01.centox.log.annotation.Log) || @annotation(io.github.sagirikawaii01.centox.log.annotation.Log))")
    public void logCut() {

    }

    @Around(value = "logCut()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long startAt = System.currentTimeMillis();
        Object keys = pjp.proceed();
        long endAt = System.currentTimeMillis();
        LogHandlerConfig handlerConfig = new LogHandlerConfigBuilder()
                .setStartAt(startAt)
                .setEndAt(endAt)
                .build();

        if (needLog(handlerConfig)) {
            if (!logHandler.beforeSaveLog(pjp)) {
                return keys;
            }
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            // 获取切入点所在的方法
            Method method = signature.getMethod();
            Log anno = method.getAnnotation(Log.class);
            if (anno == null) {
                anno = method.getDeclaringClass().getAnnotation(Log.class);
            }
            handlerConfig.setSaveRequestData(anno.isSaveRequestData());
            handlerConfig.setSaveResponseData(anno.isSaveResponseData());
            setRuntimeFromAnnotation(anno, method, handlerConfig);
            LogData logData = logHandler.handler(pjp, keys, null, handlerConfig);
            if (method.isAnnotationPresent(SaveLog.class) ||
                    anno.saveLog()
                    || method.getDeclaringClass().isAnnotationPresent(SaveLog.class)) {
                logHandler.afterSaveLog(logData);
            }
        }
        return keys;
    }

    @AfterThrowing(pointcut = "logCut()", throwing = "e")
    public void doExceptionMyLog(JoinPoint jp, Throwable e) {
        LogData logData = logHandler.handler(jp, null, e, new LogHandlerConfigBuilder().build());
        MethodSignature signature = (MethodSignature) jp.getSignature();
        // 获取切入点所在的方法
        Method method = signature.getMethod();
        Log anno = method.getAnnotation(Log.class);
        if (anno == null) {
            anno = method.getDeclaringClass().getAnnotation(Log.class);
        }
        if (method.isAnnotationPresent(SaveLog.class) ||
                anno.saveLog()
                || method.getDeclaringClass().isAnnotationPresent(SaveLog.class)) {
            logHandler.afterSaveLog(logData);
        }
    }

    private void setRuntimeFromAnnotation(Log anno, Method method, LogHandlerConfig handlerConfig) {
        if (anno.runTime() == -1) {
            handlerConfig.setRunTime(anno.runTime());
        }
    }

    protected boolean needLog(LogHandlerConfig handlerConfig) {
        return handlerConfig.getEndAt() - handlerConfig.getStartAt() >= handlerConfig.getRunTime();
    }


    public static class LogConfig {
        private LogConfig(Long runTime, Boolean excFullShow, Integer resultLength){
            this.runTime = runTime;
            this.excFullShow = excFullShow;
            this.resultLength = resultLength;
        }
        private final Long runTime;
        private final Boolean excFullShow;
        private final Integer resultLength;

        public Long getRunTime() {
            return runTime;
        }

        public Boolean getExcFullShow() {
            return excFullShow;
        }

        public Integer getResultLength() {
            return resultLength;
        }
    }

    public static class LogConfigBuilder {
        private Long runTime = null;
        private Boolean excFullShow = null;
        private Integer logLength = null;
        public LogConfigBuilder() {

        }
        public LogConfigBuilder setRunTime(Long runTime) {
            if (null == runTime) {
                runTime = 0L;
            }
            this.runTime = runTime;
            return this;
        }

        public LogConfigBuilder setExcFullShow(Boolean excFullShow) {
            if (null == excFullShow) {
                excFullShow = true;
            }
            this.excFullShow = excFullShow;
            return this;
        }

        public LogConfigBuilder setLogLength(Integer logLength) {
            if (null == logLength) {
                logLength = 0;
            }
            this.logLength = logLength;
            return this;
        }

        public LogConfig build() {
            return new LogConfig(runTime, excFullShow, logLength);
        }
    }

    private class LogHandlerConfigBuilder {
        private LogHandlerConfigBuilder(){}
        private Long startAt = 0L;
        private Long endAt = 0L;
        private boolean isSaveRequestData;
        private boolean isSaveResponseData;
        public LogHandlerConfigBuilder setStartAt(Long startAt) {
            this.startAt = startAt;
            return this;
        }

        public LogHandlerConfigBuilder setEndAt(Long endAt) {
            this.endAt = endAt;
            return this;
        }
        public LogHandlerConfigBuilder setSaveRequest(boolean isSaveRequestData) {
            this.isSaveRequestData = isSaveRequestData;
            return this;
        }

        public LogHandlerConfigBuilder setSaveResponse(boolean isSaveResponseData) {
            this.isSaveResponseData = isSaveResponseData;
            return this;
        }

        public LogHandlerConfig build() {
            return new LogHandlerConfig(objectMapper, logConfig.runTime, logConfig.excFullShow, logConfig.resultLength, this.startAt, this.endAt, this.isSaveRequestData, this.isSaveResponseData);
        }
    }
}
