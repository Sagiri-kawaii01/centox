package io.github.sagirikawaii01.centox.log;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 16:16
 * @since
 */
public class CentoxLogProperties {
    private boolean enable = true;

    /**
     * 方法的运行时长  当方法的运行时间 >= 设置的值时，才记录。 默认为-1
     */
    private long runTime = -1L;

    /**
     * 异常的堆栈信息 是否全部展示
     */
    private boolean excFullShow = false;

    /**
     * 输出日志结果的长度  0 表示全部输出
     */
    private int logLength = 0;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public long getRunTime() {
        return runTime;
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
    }

    public boolean isExcFullShow() {
        return excFullShow;
    }

    public void setExcFullShow(boolean excFullShow) {
        this.excFullShow = excFullShow;
    }

    public int getLogLength() {
        return logLength;
    }

    public void setLogLength(int logLength) {
        this.logLength = logLength;
    }
}
