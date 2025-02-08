package io.github.sagirikawaii01.centox.log;

import io.github.sagirikawaii01.centox.log.pojo.LogData;
import io.github.sagirikawaii01.centox.log.pojo.LogHandlerConfig;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.1
 */
public interface LogHandler {
    LogData handler(JoinPoint jp, Object keys, Throwable e, LogHandlerConfig config);
    boolean beforeSaveLog(ProceedingJoinPoint pjp);
    void afterSaveLog(LogData logData);

    void setExtraDataFromRequest(HttpServletRequest request, LogData logData);
}
