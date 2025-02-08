package io.github.sagirikawaii01.centox.starter;

import io.github.sagirikawaii01.centox.core.properties.ApiResponseProperties;
import io.github.sagirikawaii01.centox.core.properties.DateTimeProperties;
import io.github.sagirikawaii01.centox.core.properties.PageProperties;
import io.github.sagirikawaii01.centox.log.CentoxLogProperties;
import io.github.sagirikawaii01.centox.mp.MpProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0
 */
@ConfigurationProperties("centox")
public class CentoxProperties {

    private boolean enable = true;
    private String ann = "Json-Api-Response";


    /**
     * log config
     */
    @NestedConfigurationProperty
    private CentoxLogProperties log = new CentoxLogProperties();

    public CentoxLogProperties getLog() {
        return log;
    }

    public void setLog(CentoxLogProperties log) {
        this.log = log;
    }

    /**
     * Date time config
     */
    @NestedConfigurationProperty
    private DateTimeProperties datetime = new DateTimeProperties();

    public DateTimeProperties getDatetime() {
        return datetime;
    }

    public void setDatetime(DateTimeProperties datetime) {
        this.datetime = datetime;
    }

    /**
     * response config
     */
    @NestedConfigurationProperty
    private ApiResponseProperties response = new ApiResponseProperties();

    /**
     * page config
     */
    @NestedConfigurationProperty
    private PageProperties page = new PageProperties();

    /**
     * Mybatis plus config
     */
    @NestedConfigurationProperty
    private MpProperties mp = new MpProperties();

    public MpProperties getMp() {
        return mp;
    }

    public void setMp(MpProperties mp) {
        this.mp = mp;
    }

    public PageProperties getPage() {
        return page;
    }

    public void setPage(PageProperties page) {
        this.page = page;
    }

    public ApiResponseProperties getResponse() {
        return response;
    }

    public void setResponse(ApiResponseProperties response) {
        this.response = response;
    }

    public String getAnn() {
        return ann;
    }

    public void setAnn(String ann) {
        this.ann = ann;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
