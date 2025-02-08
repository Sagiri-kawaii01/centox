package io.github.sagirikawaii01.centox.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0
 */
public class PageProperties {

    /**
     * Page parameters of request
     */
    private RequestFields requestFields = new RequestFields();

    /**
     * Page parameters of response
     */
    private ResponseFields responseFields = new ResponseFields();

    public RequestFields getRequestFields() {
        return requestFields;
    }

    public void setRequestFields(RequestFields requestFields) {
        this.requestFields = requestFields;
    }

    public ResponseFields getResponseFields() {
        return responseFields;
    }

    public void setResponseFields(ResponseFields responseFields) {
        this.responseFields = responseFields;
    }

    public static class RequestFields {

        private String currentPage = "currentPage";
        private String pageSize = "pageSize";

        /**
         * Request Data path in the json body
         */
        private String subBodyPath = "";

        public String getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(String currentPage) {
            this.currentPage = currentPage;
        }

        public String getPageSize() {
            return pageSize;
        }

        public void setPageSize(String pageSize) {
            this.pageSize = pageSize;
        }

        public String getSubBodyPath() {
            return subBodyPath;
        }

        public void setSubBodyPath(String subBodyPath) {
            this.subBodyPath = subBodyPath;
        }
    }

    public static class ResponseFields {
        private String pageCount = "pageCount";
        private String dataCount = "dataCount";

        public String getPageCount() {
            return pageCount;
        }

        public void setPageCount(String pageCount) {
            this.pageCount = pageCount;
        }

        public String getDataCount() {
            return dataCount;
        }

        public void setDataCount(String dataCount) {
            this.dataCount = dataCount;
        }
    }
}
