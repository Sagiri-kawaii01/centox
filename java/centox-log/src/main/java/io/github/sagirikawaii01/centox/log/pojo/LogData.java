package io.github.sagirikawaii01.centox.log.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.1
 */
public class LogData {
    private static final DateTimeFormatter SDF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * token中的信息
     */
    private Map<String, Object> extraInfo;

    /**
     * 请求的类
     */
    private String className;

    /**
     * 请求的方法
     */
    private String methodName;

    /**
     * 请求的参数
     */
    private String params;

    /**
     * 返回值
     */
    private String returnValue;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 接口名称
     */
    private String apiName;


    /**
     * 接口访问状态
     */
    private RequestStatus status;


    /**
     * 是否保存request数据
     */
    private boolean isSaveRequestData;

    /**
     * 是否保存response数据
     */
    private boolean isSaveResponseData;

    /**
     * 请求的路径
     */
    private String uri;

    /**
     * 请求的ip地址
     */
    private String ip;

    /**
     * 请求的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reqTime;

    /**
     * 执行的时长
     */
    private Long execTime;

    /**
     * 异常名称
     */
    private String excName;

    /**
     * 异常信息
     */
    private String excInfo;


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }


    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }


    public boolean isSaveRequestData() {
        return isSaveRequestData;
    }

    public void setSaveRequestData(boolean saveRequestData) {
        isSaveRequestData = saveRequestData;
    }

    public boolean isSaveResponseData() {
        return isSaveResponseData;
    }

    public void setSaveResponseData(boolean saveResponseData) {
        isSaveResponseData = saveResponseData;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LocalDateTime getReqTime() {
        return reqTime;
    }

    public void setReqTime(LocalDateTime reqTime) {
        this.reqTime = reqTime;
    }

    public Long getExecTime() {
        return execTime;
    }

    public void setExecTime(Long execTime) {
        this.execTime = execTime;
    }

    public String getExcName() {
        return excName;
    }

    public void setExcName(String excName) {
        this.excName = excName;
    }

    public String getExcInfo() {
        return excInfo;
    }

    public void setExcInfo(String excInfo) {
        this.excInfo = excInfo;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public Map<String, Object> getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Map<String, Object> extraInfo) {
        this.extraInfo = extraInfo;
    }

    @Override
    public String toString() {
        return "LogData[保存请求参数/返回值][" +
                (isSaveRequestData ? "Y/" : "N/") +
                (isSaveResponseData ? "Y" : "N") + "]{" +
                "模块名='" + module + '\'' +
                ((apiName != null && !apiName.isEmpty()) ? ", 接口名称='" + apiName + "'" : "") +
                ", 接口访问状态='" + status.getName() + "'" +
                ", uri='" + uri + '\'' +
                ", ip='" + ip + '\'' +
                (isSaveRequestData ? ", 请求参数='" + params + '\'' : "")  +
                (isSaveResponseData ? ", 返回值='" + returnValue + '\'' : "") +
                ((extraInfo != null && !extraInfo.isEmpty()) ? ", 其他信息='" + extraInfo + "'" : "") +
                ", 请求发起时间='" + reqTime.format(SDF) + "'" +
                ", 请求花费时间='" + execTime + "ms'" +
                (status.equals(RequestStatus.Success) ? "" : ", 异常名称='" + excName + "', " + "异常信息='" + excInfo + "'") +
                ", controller='" + className + '\'' +
                ", 接口方法='" + methodName + '\'' +
                '}';
    }
}
