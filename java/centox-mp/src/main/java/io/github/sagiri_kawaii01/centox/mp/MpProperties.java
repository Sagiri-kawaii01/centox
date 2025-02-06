package io.github.sagiri_kawaii01.centox.mp;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 14:59
 * @since
 */
public class MpProperties {
    private String createTimeField = "gmtCreate";
    private String updateTimeField = "gmtModify";

    public String getCreateTimeField() {
        return createTimeField;
    }

    public void setCreateTimeField(String createTimeField) {
        this.createTimeField = createTimeField;
    }

    public String getUpdateTimeField() {
        return updateTimeField;
    }

    public void setUpdateTimeField(String updateTimeField) {
        this.updateTimeField = updateTimeField;
    }
}
