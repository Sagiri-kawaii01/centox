package io.github.sagiri_kawaii01.centox.core.properties;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class ApiResponseProperties {
    /**
     * Field name of response code
     */
    private String codeName = "code";

    /**
     * Field name of response message
     */
    private String messageName = "message";

    /**
     * Field name of response data
     */
    private String dataName = "data";

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }
}
