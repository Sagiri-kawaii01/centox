package io.github.sagirikawaii01.centox.log.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.1
 */
public class LogHandlerConfig {
    private ObjectMapper objectMapper;
    private Long runTime;
    private Boolean excFullShow;
    private Integer resultLength;
    private boolean isSaveRequestData;
    private boolean isSaveResponseData;
    private Long startAt;
    private Long endAt;

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

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }

    public Boolean getExcFullShow() {
        return excFullShow;
    }

    public void setExcFullShow(Boolean excFullShow) {
        this.excFullShow = excFullShow;
    }

    public Integer getResultLength() {
        return resultLength;
    }

    public void setResultLength(Integer resultLength) {
        this.resultLength = resultLength;
    }

    public Long getStartAt() {
        return startAt;
    }

    public void setStartAt(Long startAt) {
        this.startAt = startAt;
    }

    public Long getEndAt() {
        return endAt;
    }

    public void setEndAt(Long endAt) {
        this.endAt = endAt;
    }

    public LogHandlerConfig(ObjectMapper objectMapper, Long runTime, Boolean excFullShow, Integer resultLength, Long startAt, Long endAt, boolean isSaveRequestData, boolean isSaveResponseData) {
        this.objectMapper = objectMapper;
        this.runTime = runTime;
        this.excFullShow = excFullShow;
        this.resultLength = resultLength;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isSaveRequestData = isSaveRequestData;
        this.isSaveResponseData = isSaveResponseData;
    }
}